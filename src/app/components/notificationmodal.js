// components/NotificationModal.js
import React, { useRef, useEffect } from 'react';
import styles from '../styles/NotificationModal.module.css';

const NotificationModal = ({ isOpen, onClose, notifications, anchorEl }) => {
  const modalRef = useRef(null);

  useEffect(() => {
    if (isOpen && anchorEl && modalRef.current) {
      const anchorRect = anchorEl.getBoundingClientRect();
      const modalRect = modalRef.current.getBoundingClientRect();
      
      modalRef.current.style.top = `${anchorRect.bottom + window.scrollY}px`;
      modalRef.current.style.left = `${anchorRect.left}px`;
    }
  }, [isOpen, anchorEl]);

  if (!isOpen) return null;

  return (
    <div className={styles.modalOverlay} onClick={onClose}>
      <div ref={modalRef} className={styles.modalContent} onClick={e => e.stopPropagation()}>
        <div className={styles.arrow}></div>
        <h2>알림</h2>
        {notifications.length > 0 ? (
          <ul className={styles.notificationList}>
            {notifications.map((notification, index) => (
              <li key={index} className={styles.notificationItem}>
                {notification.text}
              </li>
            ))}
          </ul>
        ) : (
          <p>새로운 알림이 없습니다.</p>
        )}
        <button className={styles.closeButton} onClick={onClose}>닫기</button>
      </div>
    </div>
  );
};

export default NotificationModal;