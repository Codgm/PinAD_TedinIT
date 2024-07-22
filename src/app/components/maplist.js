'use client'
import Image from 'next/image';
import React, { useEffect, useState } from 'react';
import WritePinStory from './writepinstory';
import PinstoryModal from './pinstorymodal';

function formatDateToString(date) {
  // 연, 월, 일, 시, 분, 초를 추출
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  // 포맷팅하여 반환
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

const MapList = ({ pins,showAllPins }) => {
  // const [pins, setPins] = useState([
  //   { id: 1, x: 5, y: 5, time: '2024-07-19 09:30:00', isUser: true },
  //   { id: 2, x: 2, y: 3, time: '2024-07-19 10:00:00', isUser: false },
  //   { id: 3, x: 8, y: 7, time: '2024-07-19 10:04:00', isUser: true },
  // ]);

  const [isWriteModalOpen, setIsWriteModalOpen] = useState(false);
  const [isPinModalOpen, setIsPinModalOpen] = useState(false);
  const [selectedPin, setSelectedPin] = useState(null);
  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  // const openWriteModal = () => setIsWriteModalOpen(true);
  // const closeWriteModal = () => setIsWriteModalOpen(false);

  const openPinModal = (pin) => {
    setSelectedPin(pin);
    setIsPinModalOpen(true);
  };

  const closePinModal = () => setIsPinModalOpen(false);


  // const addPin = () => {
  //   const now = new Date();
  //   const utc = now.getTime() + (now.getTimezoneOffset() * 60 * 1000);
  //   const koreaTimeDiff = 9 * 60 * 60 * 1000;
  //   const korNow = new Date(utc+koreaTimeDiff)
  //   const formattedDate = formatDateToString(korNow);

  //   const newPin = {
  //     id: pins.length + 1,
  //     x: Math.floor(Math.random() * 10),
  //     y: Math.floor(Math.random() * 10),
  //     time: formattedDate,
  //     isUser: true,
  //   };
  //   setPins([...pins, newPin]);
  // };

  const getTimeDifference = (pinTime) => {
    const diff = currentTime - new Date(pinTime);
    const minutes = Math.floor(diff / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);
    return `${minutes}분 ${seconds}초 전`;
  };

  return (
    <div className="h-full w-full overflow-hidden flex flex-col">
      {isPinModalOpen && (
          <PinstoryModal pin={selectedPin} onClose={closePinModal} updatePin={null} />
      )}
      {/* {isWriteModalOpen && (
        <WritePinStory isOpen={isWriteModalOpen} closeModal={closeWriteModal} addPin={addPin} />
      )} */}
      <Image
        src="/world-map.webp"
        alt="Map"
        fill
        style={{ objectFit: 'cover'}}
      />
      <div className="relative flex-1 overflow-y-auto px-4 pb-4">
        {pins.filter(pin => showAllPins || pin.isUser).map((pin) => (
          <div 
            key={pin.id} 
            className="absolute w-8 h-8 flex items-center justify-center transition-all duration-300 ease-in-out"
            style={{
              left: `${pin.x * 10}%`,
              top: `${pin.y * 10}%`,
              transform: 'translate(-50%, -50%)',
              cursor: 'pointer',
            }}
            onClick={() => openPinModal(pin)}
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
