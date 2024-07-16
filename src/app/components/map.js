'use client';
import React, { useState } from 'react';
import Image from 'next/image';
import PinstoryModal from './pinstorymodal';
import WritePinStory from './writepinstory';

const Map = ({ data }) => {
  const [pins, setPins] = useState([]);
  const [isWriteModalOpen, setIsWriteModalOpen] = useState(false);
  const [isPinModalOpen, setIsPinModalOpen] = useState(false);
  const [selectedPin, setSelectedPin] = useState(null);

  const openWriteModal = () => setIsWriteModalOpen(true);
  const closeWriteModal = () => setIsWriteModalOpen(false);

  const openPinModal = (pin) => {
    setSelectedPin(pin);
    setIsPinModalOpen(true);
  };
  const closePinModal = () => setIsPinModalOpen(false);

  const handleAddPin = (newPin) => {
    setPins([...pins, { ...newPin, id: pins.length }]);
  };

  const updatePin = (updatedPin) => {
    setPins((prevPins) => prevPins.map((pin) => (pin.id === updatedPin.id ? updatedPin : pin)));
    setSelectedPin(updatedPin);
  };

  return (
    <div className="map relative" style={{height: "75vh;"}}>
      {isPinModalOpen && (
        <PinstoryModal pin={selectedPin} onClose={closePinModal} updatePin={updatePin} />
      )}
      {isWriteModalOpen && (
        <WritePinStory isOpen={isWriteModalOpen} closeModal={closeWriteModal} addPin={handleAddPin} />
      )}
      <div className="absolute inset-0">
        <Image
          src="/world-map.webp"
          alt="Map"
          fill
          style={{ objectFit: 'cover', borderRadius: '20px' }}
        />
        {pins.map((pin, index) => (
          <div
            key={index}
            className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-red-500 text-white px-2 py-1 rounded cursor-pointer"
            onClick={() => openPinModal(pin)}
          >
            Pin: {index + 1}
          </div>
        ))}
      </div>
      <div className="fixed bottom-4 md:left-2/5 transform -translate-x-1/2">
        <button
          className="inline-flex items-center justify-center rounded-full bg-purple-400 hover:bg-purple-500 shadow-lg text-white transition-colors duration-300 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 disabled:pointer-events-none disabled:opacity-50 h-14 w-14"
          onClick={openWriteModal}
        >
          <svg
            className="w-6 h-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
            aria-hidden="true"
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v6m3-3H9m12 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"></path>
          </svg>
        </button>
      </div>
    </div>
  );
};

export default Map;
