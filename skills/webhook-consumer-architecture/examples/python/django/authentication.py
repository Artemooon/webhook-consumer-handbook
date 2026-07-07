import base64

from rest_framework import exceptions
from rest_framework.authentication import BaseAuthentication


class WebhookBasicAuthentication(BaseAuthentication):
    expected_username = None
    expected_password = None

    def authenticate(self, request):
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Basic "):
            raise exceptions.AuthenticationFailed("Invalid basic authentication credentials")

        encoded = auth_header.split(" ", 1)[1]

        try:
            decoded = base64.b64decode(encoded).decode("utf-8")
            username, password = decoded.split(":", 1)
        except (TypeError, ValueError, UnicodeDecodeError):
            raise exceptions.AuthenticationFailed("Invalid basic authentication credentials")

        if username != self.expected_username or password != self.expected_password:
            raise exceptions.AuthenticationFailed("Invalid username or password")

        return None, None
