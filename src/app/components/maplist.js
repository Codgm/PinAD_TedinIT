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
              position: 'absolute',
              width: '2rem',
            }}
            onClick={() => openPinModal(pin)}
            onMouseEnter={() => handleMouseEnter(pin.id)}
            onMouseLeave={handleMouseLeave}
          >
            <div 
              className={`relative flex items-center justify-center w-16 h-16 transition-transform duration-300
              ${hoveredPinId === pin.id ? 'scale-150' : 'scale-100'}`}
            >
              <svg 
                xmlns="http://www.w3.org/2000/svg" 
                viewBox="0 0 32 32"
                className="w-16 h-16"
              >

              <circle
                  cx={32 / 2}
                  cy={32 / 2}
                  r={16 / 2}
                  fill="rgba(255, 255, 255, 0.5)" // 하양색에 50% 투명도 설정
                  stroke="rgba(0, 0, 0, 0.3)" // 검정색, 매우 얇은 테두리
                  strokeWidth={1.5} // 매우 얇은 테두리 두께
                />

                <circle
                  cx={32 / 2}
                  cy={32 / 2}
                  r={16 / 2}
                  fill="rgba(255, 255, 255, 0.5)" // 하양색에 50% 투명도 설정
                  stroke={pin.type?.[0] === '광고' ? 'rgba(255, 182, 182, 1)' : 'rgba(173, 216, 230, 1)'} // 연한 붉은색 또는 연한 하늘색
                  strokeWidth={1}    // 테두리 두께 (2px로 설정)
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
                        width: '0.9rem',
                        height: '0.9rem',
                        borderRadius: '50%',
                        top: '50%',
                        left: '50%',
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
