from rest_framework.response import Response
from rest_framework.views import APIView


class ProviderWebhookView(APIView):
    authentication_classes = []

    def post(self, request):
        delivery = webhook_delivery_service.register(
            provider="chargebee",
            headers=request.headers,
            raw_body=request.body,
        )

        process_webhook_delivery.delay(delivery.id)
        return Response(status=202)
