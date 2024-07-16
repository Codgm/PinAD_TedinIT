import React, { useState } from 'react';
import styles from '../styles/PinstoryModal.module.css';

const PinstoryModal = ({ pin, onClose, updatePin }) => {
  const [likeCount, setLikeCount] = useState(pin.likes || 0);
  const [comments, setComments] = useState(pin.comments || []);
  const [newComment, setNewComment] = useState('');

  const handleLike = () => {
    const updatedLikes = likeCount + 1;
    setLikeCount(updatedLikes);
    updatePin({ ...pin, likes: updatedLikes });
  };

  const handleCommentChange = (e) => setNewComment(e.target.value);

  const handleAddComment = () => {
    if (newComment.trim()) {
      const updatedComments = [...comments, newComment];
      setComments(updatedComments);
      setNewComment('');
      updatePin({ ...pin, comments: updatedComments });
    }
  };

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent}>
        <div className={styles.modalHeader}>
          <h2>사용자의 핀스토리</h2>
          <button className={styles.closeButton} onClick={onClose}>×</button>
        </div>
        <div className={styles.imageContainer}>
          {pin.image ? (
            <img src={URL.createObjectURL(pin.image)} alt="Pin Image" className={styles.image} />
          ) : (
            <div className={styles.placeholderImage}>400 x 300</div>
          )}
        </div>
        <p className={styles.content}>{pin.content || '핀스토리 내용이 없습니다.'}</p>
        <div className={styles.interactionBar}>
          <button className={styles.likeButton} onClick={handleLike}>
            <span role="img" aria-label="heart">♡</span> {likeCount}
          </button>
          <button className={styles.commentButton}>
            <span role="img" aria-label="comment">💬</span> {comments.length}
          </button>
        </div>
        <div className={styles.commentSection}>
          <textarea
            className={styles.commentInput}
            placeholder="댓글 작성..."
            value={newComment}
            onChange={handleCommentChange}
          ></textarea>
          <button className={styles.addCommentButton} onClick={handleAddComment}>댓글 달기</button>
        </div>
        <div className={styles.commentsList}>
          {comments.map((comment, index) => (
            <div key={index} className={styles.comment}>
              {comment}
            </div>
          ))}
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
