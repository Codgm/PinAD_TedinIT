'use client'
import React, { useEffect, useState } from 'react';
import Image from 'next/image';
import PinLibraryModal from './pinlibrary';

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
              position: 'relative',
              width: '2rem',
            }}
            onClick={() => openPinModal(pin)}
            onMouseEnter={() => handleMouseEnter(pin.id)}
            onMouseLeave={handleMouseLeave}
          >
            <div 
              className={`relative flex items-center justify-center w-14 h-14
              ${hoveredPinId === pin.id ? 'scale-150' : 'scale-100'}`}
            >
              <svg 
                xmlns="http://www.w3.org/2000/svg" 
                viewBox="0 0 48 48"
                className="absolute w-14 h-14"
                style={{ fill: pin.type?.[0] === '광고' ? 'red' : 'blue' }} // 핀 배경색
              >
                <path 
                  d="M24.43,40.153c0.186,0.276,0.497,0.441,0.829,0.441h0c0.333,0,0.644-0.166,0.829-0.441l9.436-14.018c1.128-1.799,1.725-3.865,1.725-5.976c0-6.376-5.383-11.564-12-11.564s-12,5.188-12,11.564c0,2.114,0.599,4.184,1.749,6.012L24.43,40.153z M25.25,10.594c5.514,0,10,4.291,10,9.564c0,1.734-0.491,3.433-1.402,4.886l-8.59,12.76l-8.584-12.726c-0.932-1.482-1.424-3.183-1.424-4.92C15.25,14.885,19.736,10.594,25.25,10.594z"
                />
                <path 
                  d="M25.25,25.616c3.135,0,5.686-2.467,5.686-5.5s-2.55-5.5-5.686-5.5s-5.686,2.467-5.686,5.5S22.115,25.616,25.25,25.616z M25.25,16.616c2.032,0,3.686,1.57,3.686,3.5s-1.653,3.5-3.686,3.5s-3.686-1.57-3.686-3.5S23.218,16.616,25.25,16.616z"
                />
                {pin.profile && pin.profile.avatar && (
                  <foreignObject
                    x="0" 
                    y="0" 
                    width="100%" 
                    height="100%"
                  >
                    <img
                      src={pin.profile.avatar}
                      alt="Profile"
                      style={{
                        position: 'absolute',
                        width: '1.25rem',
                        height: '1.25rem',
                        borderRadius: '50%',
                        top: '42%',
                        left: '52.275%',
                        transform: 'translate(-50%, -50%)',
                        objectFit: 'cover'
                      }}
                    />
                  </foreignObject>
                )}
              </svg>
            </div>
            {hoveredPinId === pin.id && ( // 큰 사각형 렌더링
              <div className="absolute -top-24 left-1/2 transform -translate-x-1/2 bg-gray-500 opacity-50 w-24 h-24 rounded-lg z-10 transition-opacity duration-500">
                {/* 필요시 이곳에 추가 내용 또는 스타일을 넣을 수 있습니다 */}
              </div>
            )}
            {hoveredPinId === pin.id && ( // 시간 표시 조건부 렌더링
              <div className={`absolute top-full left-1/2 transform -translate-x-1/2 text-xs font-semibold bg-white px-2 py-1 rounded-full shadow-md whitespace-nowrap transition-opacity duration-  ${hoveredPinId === pin.id ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2'}`}>
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
