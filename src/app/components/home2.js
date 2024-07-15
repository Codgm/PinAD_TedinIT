'use client'
// pages/index.js
import Head from 'next/head';
import styles from '../styles/Home.module.css';
import Map from '@/app/components/map';
import { useRef, useState } from 'react';
import NotificationModal from './notificationmodal';
import PopularPinStory from './popularpinstory';
import RecentPinStory from './recentpinstory';

export default function Home2() {

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [notifications] = useState([
    "새로운 여행 추천이 도착했습니다!",
    "친구가 당신의 스토리를 좋아합니다.",
    "이번 주 인기 여행지: 제주도"
  ]);
  const notificationIconRef = useRef(null);

  const handleNotificationClick = () => {
    setIsModalOpen(true);
  };


  return (
    <div className={styles.pageWrapper}>
      <Head>
        <title>Pinstory</title>
        <link rel="icon" href="/favicon.ico" />
      </Head>

      <div className={styles.container}>
        <header className={styles.header}>
          <h1>Pinstory</h1>
          <div className={styles.icons}>
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
        </header>

        <main className={styles.main}>
          <div className='w-1/2'>
          <section className={styles.imageSection}>
            <Map/>
          </section>
          </div>

          <section className={styles.contentSection}>
            <div className={styles.searchBar}>
              <input type="text" placeholder="장소, 인물, 스토리 검색" />
            </div>

            <div className={styles.tags}>
              {['#여행추천', '#숨은명소', '#가성비', '#내돈내산', '#스폰서찾기'].map(tag => (
                <span key={tag} className={styles.tag}>{tag}</span>
              ))}
            </div>

            <div className={styles.buttons}>
              <button>인기 핀스토리</button>
              <button>이전 읽은 핀스토리</button>
            </div>
            <PopularPinStory/>
            <RecentPinStory/>
          </section>
        </main>
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