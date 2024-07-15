'use client'
import React, { useState } from 'react';
import Image from 'next/image';
import PinstoryModal from './pinstorymodal';


const Map = () => {
  const [pins, setPins] = useState({});
  const [isModalOpen, setIsModalOpen] = useState(false);
  const handleCloseModal = () => {
    setIsModalOpen(false);
  };
  const handleModalOpen = () => {
    setIsModalOpen(true);
  }

  const handleAddPin = () => {
    setPins((prevPins) => {
      const newPins = { ...prevPins };
      if (!newPins['pin1']) {
        newPins['pin1'] = 1;
      } else {
        newPins['pin1'] += 1;
      }
      return newPins;
    });
  };

  return (
    <div className="map w-full h-full rounded-full">
      {isModalOpen && <PinstoryModal onClose={handleCloseModal}></PinstoryModal>}
      <div className="absolute inset-0 overflow-hidden">
        <Image
          src="/world-map.webp"
          alt="Map"
          fill
          style={{ objectFit: 'cover' ,borderRadius:' 20px ' }}
        />
        {pins['pin1'] && (
          <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-red-500 text-white px-2 py-1 rounded" onClick={handleModalOpen}>
            Pin: {pins['pin1']}
          </div>
        )}
      </div>
      <div className='fixed bottom-4 md:left-2/5 transform -translate-x-1/2'>
        <button 
          className="inline-flex items-center justify-center rounded-full bg-purple-400 hover:bg-purple-500 shadow-lg text-white transition-colors duration-300 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 disabled:pointer-events-none disabled:opacity-50 h-14 w-14 absolute bottom-8 left-1/2 transform -translate-x-1/2"
          onClick={handleAddPin}
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
