'use client'
import Image from 'next/image';
import React, { useEffect, useState } from 'react';


const MapList = ({showAllPins}) => {

  const [pins, setPins] = useState([
    { id: 1, x: 5, y: 5, time: '2024-07-19 9:30:00', isUser: true },
    { id: 2, x: 2, y: 3, time: '2024-07-19 10:00:00', isUser: false },
    { id: 3, x: 8, y: 7, time: '2024-07-19 10:04:00', isUser: true },
  ]);



  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);


  const addPin = () => {
    const newPin = {
      id: pins.length + 1,
      x: Math.floor(Math.random() * 10),
      y: Math.floor(Math.random() * 10),
      time: new Date().toISOString().slice(0, 19).replace('T', ' '),
      isUser: true,
    };
    setPins([...pins, newPin]);
  };

  const getTimeDifference = (pinTime) => {
    const diff = currentTime - new Date(pinTime);
    const minutes = Math.floor(diff / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);
    return `${minutes}분 ${seconds}초 전`;
  };


  return (
    <div className="h-full w-full overflow-hidden flex flex-col">
      <Image
        src="/world-map.webp"
        alt="Map"
        fill
        style={{ objectFit: 'cover', borderRadius: '20px' }}
      />
      <div className="flex-1 overflow-y-auto px-4 pb-4">
      
      {pins.filter(pin => showAllPins || pin.isUser).map((pin) => (
          <div 
            key={pin.id} 
            className="absolute w-8 h-8 flex items-center justify-center transition-all duration-300 ease-in-out"
            style={{
              left: `${pin.x * 10}%`,
              top: `${pin.y * 10}%`,
              transform: 'translate(-50%, -50%)',
            }}
          >
            <div 
              className={`w-6 h-6 rounded-full ${pin.isUser ? 'bg-red-500' : 'bg-blue-500'} shadow-lg pulse`}
            />
            <div className="absolute top-full left-1/2 transform -translate-x-1/2 text-xs font-semibold bg-white px-2 py-1 rounded-full shadow-md whitespace-nowrap">
              {pin.id === pins[pins.length - 1].id 
                ? getTimeDifference(pin.time)
                : pin.time.split(' ')[1]}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default MapList;