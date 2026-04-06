import React from "react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";

// Example hotel data (replace with your database/API data)
const hotels = [
  { id: 1, name: "Hotel LuxNes A", lat: 28.6139, lng: 77.209 }, // Delhi
  { id: 2, name: "Hotel LuxNes B", lat: 19.076, lng: 72.8777 }, // Mumbai
  { id: 3, name: "Hotel LuxNes C", lat: 12.9716, lng: 77.5946 }, // Bangalore
];

function ViewHotels() {
  return (
    <div style={{ height: "100vh", width: "100%" }}>
      <MapContainer center={[20.5937, 78.9629]} zoom={5} style={{ height: "100%", width: "100%" }}>
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />

        {hotels.map((hotel) => (
          <Marker key={hotel.id} position={[hotel.lat, hotel.lng]}>
            <Popup>{hotel.name}</Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
}

export default ViewHotels;
