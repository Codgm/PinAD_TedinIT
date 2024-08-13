'use client';
import NavBar from '@/app/components/navbar';
import styles from '@/app/styles/Layout.module.css'; 

export default function Layout({ children, onWriteModal }) {
  return (
    <div className={styles.layout}>
      <main className={styles.mainContent}>
        {children}
        <NavBar writeModal={onWriteModal} />
      </main>
    </div>
  );
}
