from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse
import json


class MockAmdocsHandler(BaseHTTPRequestHandler):
    def send_json(self, status, data):
        response_body = json.dumps(data).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(response_body)))
        self.end_headers()
        self.wfile.write(response_body)

    def read_json(self):
        length = int(self.headers.get("Content-Length", "0"))
        if length == 0:
            return {}
        raw_body = self.rfile.read(length)
        try:
            parsed = json.loads(raw_body.decode("utf-8"))
            return parsed if isinstance(parsed, dict) else {}
        except (json.JSONDecodeError, UnicodeDecodeError):
            return {}

    def get_reference(self, body):
        """Extract the transaction reference from metadata or request fields."""
        metadata = body.get("metadata", [])
        if isinstance(metadata, list):
            for item in metadata:
                if isinstance(item, dict) and item.get("key") == "description":
                    return item.get("value")
        return body.get("reference") or body.get("transactionReference")

    def do_POST(self):
        path = urlparse(self.path).path
        request_body = self.read_json()
        print(f"\nPOST {path}")
        print(f"Request body: {json.dumps(request_body)}")

        # 1. Mock authentication
        if path == "/token/oauth/resourcetoken":
            return self.send_json(200, {
                "access_token": "mock-access-token",
                "token_type": "Bearer",
                "expires_in": 3600,
            })

        # 2. Mock quotation
        if path == "/bus-server/webapi/v2/MM/quotes":
            reference = self.get_reference(request_body)
            print(f"Quotation reference: {reference}")
            if isinstance(reference, str) and reference.startswith("TXN-FAILED-"):
                print("Simulating Amdocs quotation failure")
                return self.send_json(500, {
                    "error": "AMDOCS_QUOTATION_FAILED",
                    "message": "Simulated Amdocs quotation failure",
                })
            return self.send_json(200, {
                "quotationReference": "MOCK-QUOTE-001",
                "quotationReceipt": "MOCK-RECEIPT-001",
            })

        # 3. Mock Cash-In transaction
        if path == "/bus-server/webapi/v2/MM/transactions":
            reference = self.get_reference(request_body)
            print(f"Cash-In reference: {reference}")
            if reference == "TXN-CASHIN-FAILED-001":
                print("Simulating Amdocs Cash-In failure")
                return self.send_json(500, {
                    "error": "AMDOCS_CASHIN_FAILED",
                    "message": "Simulated Cash-In execution failure",
                })
            return self.send_json(200, {
                "transactionReference": "MOCK-TXN-001",
                "transactionStatus": "completed",
                "transactionReceipt": "MOCK-CASHIN-RECEIPT",
            })

        return self.send_json(404, {"error": "Unknown mock endpoint"})

    def do_GET(self):
        path = urlparse(self.path).path
        print(f"\nGET {path}")
        if "/groups" in path:
            groups = (
                [{"name": "RET_Assujetti"}]
                if "0341234567" in path
                else [{"name": "Customers"}]
            )
            return self.send_json(200, {"agentGroups": groups})
        return self.send_json(404, {"error": "Unknown mock endpoint"})


if __name__ == "__main__":
    server = ThreadingHTTPServer(("127.0.0.1", 9090), MockAmdocsHandler)
    print("Mock Amdocs server running on http://127.0.0.1:9090")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping mock Amdocs server...")
    finally:
        server.server_close()
