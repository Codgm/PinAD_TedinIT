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
import LanguageSwitcher from './languageswitcher';
import NewUI from './hotStoryUI';
import ProfileUI from './ProfileUI';
import SearchUI from '../search/SearchUI';

export default function Home2() {
  const [isChecked, setIsChecked] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSearchBarOpen, setIsSearchBarOpen] = useState(false);
  const [isWriteModalOpen, setIsWriteModalOpen] = useState(1);

  const [notifications, setNotifications] = useState([
    { text: "새로운 여행 추천이 도착했습니다!", isRead: false },
    { text: "친구가 당신의 스토리를 좋아합니다.", isRead: false },
    { text: "이번 주 인기 여행지: 제주도", isRead: false },
  ]);

  const [pins, setPins] = useState([
    { id: 1, x: 5, y: 5, time: '2024-07-24 09:30:00', images: ["/노래방.jpg","/디저트.jpg","/삼겹살.jpg"], isUser: true, profile: { avatar: '/profileimg3.jpg', name: '김우빈' } },
    { id: 2, x: 2, y: 3, time: '2024-07-24 10:00:00', images: ["/노래방.jpg","/디저트.jpg"], isUser: false, profile: { avatar: '/profileimg2.jpg', name: '김오빈' } },
    { id: 3, x: 8, y: 7, time: '2024-07-25 09:00:00', images: ["/노래방.jpg"], isUser: true, profile: { avatar: '/profileimg3.jpg', name: '김우빈' } },
    { id: 4, x: 7, y: 6, time: '2024-07-25 09:00:00', images: ["/노래방.jpg","/디저트.jpg","/삼겹살.jpg"], isUser: false, type: ['광고'], selectedCategory: ['유통'] },
    { id: 5, x: 3, y: 4, time: '2024-07-25 12:15:00', images: ["/노래방.jpg","/디저트.jpg","/삼겹살.jpg"], isUser: false, type: ['핀스토리'], selectedCategory: ['여행기록'] },
    { id: 6, x: 6, y: 2, time: '2024-07-26 08:45:00', images: ["/노래방.jpg","/디저트.jpg","/삼겹살.jpg"], isUser: false, type: ['핀스토리'], selectedCategory: ['리뷰'] },
    { id: 7, x: 1, y: 9, time: '2024-07-26 14:30:00', images: ["/노래방.jpg","/디저트.jpg","/삼겹살.jpg"], isUser: false, type: ['핀스토리'], selectedCategory: ['명소추천'] },
    { id: 8, x: 4, y: 5, time: '2024-07-27 10:00:00', images: ["/노래방.jpg","/디저트.jpg","/삼겹살.jpg"], isUser: false, type: ['핀스토리'], selectedCategory: ['약속장소'] },
    { id: 9, x: 9, y: 3, time: '2024-07-27 16:00:00', images: ["/노래방.jpg","/디저트.jpg","/삼겹살.jpg"], isUser: false, type: ['광고'], selectedCategory: ['행사알림'] },
    { id: 10, x: 5, y: 8, time: '2024-07-28 11:00:00', images: ["/노래방.jpg","/디저트.jpg","/삼겹살.jpg"], isUser: false, type: ['광고'], selectedCategory: ['F&B'] },
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
  const closeWriteModal = () => setIsWriteModalOpen(1);
  const sssModal = (e) => setIsWriteModalOpen(e);
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

  const addPin = ({images,isUser,type,category,title,location,details}) => {
    const now = new Date();
    const utc = now.getTime() + (now.getTimezoneOffset() * 60 * 1000);
    const koreaTimeDiff = 9 * 60 * 60 * 1000;
    const korNow = new Date(utc+koreaTimeDiff)
    const formattedDate = formatDateToString(korNow);
    if(images.length === 0) images=[`/노래방.jpg`]
    console.log(images)
    console.log(isUser)
    console.log(type)
    console.log(category)
    console.log(title)
    console.log(location)
    console.log(details)
    if(title === null) title = '제목 없음';
    if(location === null) location = '위치 정보 없음';
    if(details === null) details = '상세 정보 없음';

    const newPin = {
      id: pins.length + 1,
      x: Math.floor(Math.random() * 10),
      y: Math.floor(Math.random() * 10),
      time: formattedDate,
      images: images,
      isUser: true,
      type :  [type],
      selectedCategory : [category],
      profile: { avatar: '/profileimg3.jpg', name: '김우빈' },
      title: title,
      location : location,
      details : details,
    };
    setPins([...pins, newPin]);
  };


  return (
    <div className={styles.pageWrapper}>
      {isWriteModalOpen==3 && (
        <WritePinStory isOpen={isWriteModalOpen} closeModal={closeWriteModal} addPin={addPin} />
      )}
      <div className={styles.container}>
        <main className={styles.main}>
          <section className={styles.mapSection}>
            {/* <span 
              ref={notificationIconRef}
              className={styles.notification} 
              onClick={handleNotificationClick}
            >
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
                <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z"/>
              </svg>
            </span> */}
            {/* <LanguageSwitcher></LanguageSwitcher>             */}
            {isWriteModalOpen===1 && (<label className={styles.switch}>
              <input
                type="checkbox"
                checked={isChecked}
                onChange={handleChange}
              />
              <span className={styles.slider}></span>
            </label>)}
            {isWriteModalOpen ===2 && (
              <SearchUI/>
            )
              
            }
            {/* MapList가 isWriteModalOpen이 1 또는 3일 때 보이도록 설정 */}
            {(isWriteModalOpen === 1 || isWriteModalOpen === 3) && (
              <MapList pins={pins} showAllPins={!isChecked} />
            )}

            {isWriteModalOpen === 2 && <SearchPage/>}
            
            {/* ProfileUI가 isWriteModalOpen이 5일 때 보이도록 설정 */}
            {isWriteModalOpen === 5 && <ProfileUI />}
            
            {/* NewUI가 isWriteModalOpen이 4일 때 보이도록 설정 */}
            {isWriteModalOpen === 4 && <NewUI />}
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
        <NavBar writeModal={sssModal}/>
      </div>

      {/* <NotificationModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)}
        notifications={notifications}
        anchorEl={notificationIconRef.current}
      /> */}
    </div>
  )
}