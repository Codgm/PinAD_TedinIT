'use client';
import React, { useRef, useState } from 'react';
import LanguageSwitcher from './languageswitcher';
import NotificationModal from './notificationmodal';

const ProfileUI = () => {
    const [language, setLanguage] = useState('한국어');
  const [notifications, setNotifications] = useState([
    { text: "새로운 여행 추천이 도착했습니다!", isRead: false },
    { text: "친구가 당신의 스토리를 좋아합니다.", isRead: false },
    { text: "이번 주 인기 여행지: 제주도", isRead: false },
  ]);
  const notificationIconRef = useRef(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleNotificationClick = () => {
    setIsModalOpen(true);
  };

  return (
    <div className="relative w-full h-full bg-white shadow-lg rounded-lg overflow-hidden">
      {/* Header 추가 */}
      <div className="relative bg-white text-black border-b border-gray-300 py-3 px-4">
        <h1 className="text-lg font-bold">MyPage</h1>
        <span 
              ref={notificationIconRef}
              onClick={handleNotificationClick}
              className="absolute top-2 right-2 p-2" // 오른쪽 상단에 위치
            >
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
                <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z"/>
              </svg>
            </span>
      </div>

      <div className="bg-gray-100">
        <div className="space-y-4">
            
          <div className="relative flex items-center bg-white space-x-4 border border-gray-300 p-4">
          <LanguageSwitcher/>
            <div className="w-20 h-20 bg-gray-300 rounded-full overflow-hidden">
              <img src="/profileimg3.jpg" alt="김옥빈" className="w-full h-full object-cover" />
            </div>
            <div>
              <h2 className="text-xl font-semibold">Gildong</h2>
              <p className="text-xs pl-2 pb-1 text-gray-500">gildong1234@gmail.com</p>
              <p className="text-sm pl-2 pb-2 text-gray-700">Hi. I'm Hong GilDong</p>
              <button className="w-full py-2 px-4 bg-gray-100 text-gray-800 rounded hover:bg-gray-200 flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                </svg>
                Edit Profile
              </button>
            </div>
          </div>
          {/* 포인트 구분선 추가 */}
          <div className="bg-white border-t border-gray-300 pt-4 pb-4 mt-4" >
          <div className="p-4 border-b border-gray-300 text-gray-800">
              <h2 className="text-lg font-semibold">My Point</h2>
              <p className="text-xl font-bold">1500p</p> {/* 여기에 실제 포인트를 동적으로 삽입할 수 있습니다. */}
            </div>
            <div className="w-full p-4 bg-white border-b border-gray-300 text-gray-800 hover:bg-gray-200 flex items-start justify-start">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path d="M4 4a2 2 0 00-2 2v1h16V6a2 2 0 00-2-2H4z" />
                <path fillRule="evenodd" d="M18 9H2v5a2 2 0 002 2h12a2 2 0 002-2V9zM4 13a1 1 0 011-1h1a1 1 0 110 2H5a1 1 0 01-1-1zm5-1a1 1 0 100 2h1a1 1 0 100-2H9z" clipRule="evenodd" />
              </svg>
              포인트 충전
            </div>
            <div className="w-full p-4 bg-white border-b border-gray-300 text-gray-800 hover:bg-gray-200 flex items-start justify-start">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
              플랜 가입
            </div>
            
          </div>
        </div>
      </div>      
      <NotificationModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)}
        notifications={notifications}
        anchorEl={notificationIconRef.current}
      />
    </div>
  );
};

export default ProfileUI;
