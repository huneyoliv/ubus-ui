package com.ubusmobilidade.ubus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MapPoint(val lat: Double, val lng: Double, val label: String)

@Composable
expect fun MapWebView(
    lat: Double,
    lng: Double,
    points: List<MapPoint>,
    modifier: Modifier = Modifier,
)

fun buildLeafletHtml(lat: Double, lng: Double, points: List<MapPoint>): String {
    val pointsJson = Json.encodeToString(points)
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <style>
                html, body, #map {
                    height: 100%;
                    margin: 0;
                    padding: 0;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <script>
                var map = L.map('map').setView([$lat, $lng], 13);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '© OpenStreetMap contributors'
                }).addTo(map);

                var driverMarker = L.marker([$lat, $lng]).addTo(map)
                    .bindPopup('Minha Localização')
                    .openPopup();

                var points = $pointsJson;
                var bounds = L.latLngBounds([[$lat, $lng]]);

                points.forEach(function(point) {
                    if (point.lat && point.lng) {
                        L.marker([point.lat, point.lng]).addTo(map)
                            .bindPopup(point.label);
                        bounds.extend([point.lat, point.lng]);
                    }
                });

                if (points.length > 0) {
                    map.fitBounds(bounds, { padding: [50, 50] });
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
