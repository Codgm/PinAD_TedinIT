'use client'
import React, { useEffect, useState } from 'react';
import Image from 'next/image';
import PinLibraryModal from './pinlibrary'; // 모달 컴포넌트 경로 조정 필요

function formatDateToString(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

const MapList = ({ pins, showAllPins }) => {
  const [isPinModalOpen, setIsPinModalOpen] = useState(false);
  const [selectedPin, setSelectedPin] = useState(null);
  const [currentTime, setCurrentTime] = useState(new Date());
  const [hoveredPinId, setHoveredPinId] = useState(null);
  const [hoverTimeout, setHoverTimeout] = useState(null);

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const openPinModal = (pin) => {
    setSelectedPin(pin);
    setIsPinModalOpen(true);
  };

  const closePinModal = () => setIsPinModalOpen(false);

  const getTimeDifference = (pinTime) => {
    const diff = currentTime - new Date(pinTime);
    const minutes = Math.floor(diff / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);
    return `${minutes}분 ${seconds}초 전`;
  };

  const handleMouseEnter = (pinId) => {
    // 타이머 설정
    const timeout = setTimeout(() => {
      setHoveredPinId(pinId);
    }, 500); // 1초 후에 핀을 활성화
    setHoverTimeout(timeout);
  };

  const handleMouseLeave = () => {
    // 타이머 해제
    if (hoverTimeout) {
      clearTimeout(hoverTimeout);
    }
    setHoveredPinId(null);
  };

  return (
    <div className="h-full w-full overflow-hidden flex flex-col">
      {isPinModalOpen && (
        <PinLibraryModal pin={selectedPin} onClose={closePinModal} updatePin={null} />
      )}
      <Image
        src="/gangnam.png"
        alt="Map"
        fill
        style={{ objectFit: 'cover'}}
      />
      <div className="relative flex-1 overflow-y-auto px-4 pb-4">
        {pins.filter(pin => showAllPins || pin.isUser).map((pin) => (
          <div 
            key={pin.id} 
            className="absolute flex items-center justify-center"
            style={{
              left: `${pin.x * 10}%`,
              top: `${pin.y * 10}%`,
              transform: 'translate(-50%, -50%)',
              cursor: 'pointer',
              position: 'relative'
            }}
            onClick={() => openPinModal(pin)}
            onMouseEnter={() => handleMouseEnter(pin.id)}
            onMouseLeave={handleMouseLeave}
          >
            <div 
              className={`w-6 h-6 rounded-full transition-transform duration-300 
                          ${pin.type?.[0] === '광고' ? 'bg-red-500' : 'bg-blue-500'} 
                          shadow-lg ${hoveredPinId === pin.id ? 'scale-110 shadow-2xl' : 'scale-100 shadow-lg'}`}
            >
              {pin.profile && pin.profile.avatar && (
                <img
                  src={pin.profile.avatar}
                  alt="Profile"
                  className="w-full h-full rounded-full object-cover"
                />
              )}
            </div>
            {hoveredPinId === pin.id && ( // 큰 사각형 렌더링
              <div className="absolute -top-24 left-1/2 transform -translate-x-1/2 bg-gray-500 opacity-50 w-24 h-24 rounded-lg z-10 transition-opacity duration-500">
                {/* 필요시 이곳에 추가 내용 또는 스타일을 넣을 수 있습니다 */}
              </div>
            )}
            {hoveredPinId === pin.id && ( // 시간 표시 조건부 렌더링
              <div className={`absolute top-full left-1/2 transform -translate-x-1/2 text-xs font-semibold bg-white px-2 py-1 rounded-full shadow-md whitespace-nowrap transition-opacity duration-200 ${hoveredPinId === pin.id ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2'}`}>
                {pin.id === pins[pins.length - 1].id 
                  ? getTimeDifference(pin.time)
                  : pin.time.split(' ')[1]}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default MapList;
