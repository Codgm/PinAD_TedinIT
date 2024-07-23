'use client'
// pages/index.js
import Head from 'next/head';
import styles from '../styles/Home.module.css';
import { useRef, useState } from 'react';
import NotificationModal from './notificationmodal';
import PopularRecentPinStory from './popular-recent-pinstory';
import MapList from './maplist';
import WritePinStory from './writepinstory';
import NavBar from './navbar';

export default function Home2() {
  const [isChecked, setIsChecked] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSearchBarOpen, setIsSearchBarOpen] = useState(false);
  const [isWriteModalOpen, setIsWriteModalOpen] = useState(false);

  const [notifications, setNotifications] = useState([
    { text: "새로운 여행 추천이 도착했습니다!", isRead: false },
    { text: "친구가 당신의 스토리를 좋아합니다.", isRead: false },
    { text: "이번 주 인기 여행지: 제주도", isRead: false },
  ]);

  const [pins, setPins] = useState([
    { id: 1, x: 5, y: 5, time: '2024-07-19 09:30:00',images : ["/노래방.jpg","/디저트.jpg","/삼겹살.jpg"], isUser: true },
    { id: 2, x: 2, y: 3, time: '2024-07-19 10:00:00',images :  ["/노래방.jpg","/디저트.jpg"], isUser: false },
    { id: 3, x: 8, y: 7, time: '2024-07-19 10:04:00',images :  ["/노래방.jpg"], isUser: true },
  ]);

  const handleChange = () => { 
    setIsChecked(!isChecked);
  }

  const notificationIconRef = useRef(null);

  const handleNotificationClick = () => {
    setIsModalOpen(true);
  };

  const handleSearchIconClick = () => {
    setIsSearchBarOpen(!isSearchBarOpen);
  };
  
  const openWriteModal = () => setIsWriteModalOpen(true);
  const closeWriteModal = () => setIsWriteModalOpen(false);

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

  const addPin = () => {
    const now = new Date();
    const utc = now.getTime() + (now.getTimezoneOffset() * 60 * 1000);
    const koreaTimeDiff = 9 * 60 * 60 * 1000;
    const korNow = new Date(utc+koreaTimeDiff)
    const formattedDate = formatDateToString(korNow);

    const newPin = {
      id: pins.length + 1,
      x: Math.floor(Math.random() * 10),
      y: Math.floor(Math.random() * 10),
      time: formattedDate,
      isUser: true,
    };
    setPins([...pins, newPin]);
  };


  return (
    <div className={styles.pageWrapper}>
      {isWriteModalOpen && (
        <WritePinStory isOpen={isWriteModalOpen} closeModal={closeWriteModal} addPin={addPin} />
      )}
      <div className={styles.container}>
        {/* <header className={styles.header}>
          <h1>Pinstory</h1>
          <div className={styles.icons}>
            <span
              className={styles.notification} 
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
            </span>
            <span
              className={styles.notification} 
              onClick={handleSearchIconClick}
            >
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="size-6">
                <path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z" />
              </svg>
            </span>
            <span 
              ref={notificationIconRef}
              className={styles.notification} 
              onClick={handleNotificationClick}
            >
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
                <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z"/>
              </svg>
            </span>
            <span className={styles.profile}>👤</span>
          </div>
        </header> */}

        <main className={styles.main}>
          <section className={styles.mapSection}>
          <span 
              ref={notificationIconRef}
              className={styles.notification} 
              onClick={handleNotificationClick}
            >
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
                <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z"/>
              </svg>
            </span>
            
            <label className={styles.switch}>
              <input
                type="checkbox"
                checked={isChecked}
                onChange={handleChange}
              />
              <span className={styles.slider}></span>
            </label>
            <MapList pins={pins} showAllPins={!isChecked}/>
            {/* {isChecked ? <PopularRecentPinStory/> : <MapList pins={pins} showAllPins={!isChecked} />} */}
            {isSearchBarOpen && (
              <div className={styles.searchBarModal}>
                <input type="text" placeholder="장소, 인물, 스토리 검색" />
              </div>
            )}
          </section>
          <section className={styles.contentSection}>
            {/* <div className={styles.searchBar}>
              <input type="text" placeholder="장소, 인물, 스토리 검색" />
            </div>
            <div className={styles.tags}>
              {['#여행추천', '#숨은명소', '#가성비', '#내돈내산', '#스폰서찾기'].map(tag => (
                <span key={tag} className={styles.tag}>{tag}</span>
              ))}
            </div> */}
            {/* <PopularRecentPinStory/> */}
          </section>
        </main>
        <NavBar />
      </div>

      <NotificationModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)}
        notifications={notifications}
        anchorEl={notificationIconRef.current}
      />
    </div>
  )
}