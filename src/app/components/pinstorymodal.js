// PinstoryModal.js
import React from 'react';
import styles from '../styles/PinstoryModal.module.css';

const PinstoryModal = ({ onClose }) => {
  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent}>
        <div className={styles.modalHeader}>
          <h2>사용자의 핀스토리</h2>
          <button className={styles.closeButton} onClick={onClose}>×</button>
        </div>
        <div className={styles.imageContainer}>
          <div className={styles.placeholderImage}>
            400 x 300
          </div>
        </div>
        <p className={styles.noContent}>핀스토리 내용이 없습니다.</p>
        <div className={styles.interactionBar}>
          <button className={styles.likeButton}>
            <span role="img" aria-label="heart">♡</span> 0
          </button>
          <button className={styles.commentButton}>
            <span role="img" aria-label="comment">💬</span> 0
          </button>
          <button className={styles.shareButton}>
            <span role="img" aria-label="share">↗️</span>
          </button>
        </div>
        <div className={styles.socialLinks}>
          <button className={styles.youtubeButton}>유튜브</button>
          <button className={styles.instagramButton}>인스타그램</button>
        </div>
      </div>
    </div>
  );
};

export default PinstoryModal;