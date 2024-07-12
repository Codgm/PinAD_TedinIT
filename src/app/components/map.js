'use client'
import React, { useState } from 'react';
import Image from 'next/image';

const Map = () => {
  const [pins, setPins] = useState({});

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
    <div className="relative w-[500px] h-[500px] mx-auto">
      <div className="absolute inset-0 border border-gray-300 overflow-hidden">
        <Image
          src="/world-map.webp"
          alt="Map"
          layout="fill"
          objectFit="cover"
        />
        {pins['pin1'] && (
          <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-red-500 text-white px-2 py-1 rounded">
            Pin: {pins['pin1']}
          </div>
        )}
      </div>
      <button 
        className="absolute bottom-4 right-4 w-10 h-10 bg-blue-500 text-white rounded-full text-2xl leading-none hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50"
        onClick={handleAddPin}
      >
        +
      </button>
    </div>
  );
};

export default Map;
