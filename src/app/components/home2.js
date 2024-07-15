// pages/index.js
import Head from 'next/head'
import styles from '../styles/Home.module.css'

export default function Home2() {
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
            <span className={styles.notification}>🔔</span>
            <span className={styles.profile}>👤</span>
          </div>
        </header>

        <main className={styles.main}>
          <section className={styles.imageSection}>
            {/* SVG 이미지 컴포넌트 */}
          </section>

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

            <div className={styles.recommendations}>
              <div className={styles.recommendationItem}>
                <h3>서울 야경 명소</h3>
                <p>남산에서 바라본 서울의 밤...</p>
              </div>
              <div className={styles.recommendationItem}>
                <h3>제주도 숨은 맛집</h3>
                <p>현지인만 아는 흑돼지 맛집...</p>
              </div>
            </div>
          </section>
        </main>
      </div>
    </div>
  )
}