import React from 'react';
import styles from '@/app/styles/Recommendations.module.css';

const RecentPinStory = () => {
    return (
        <div className={`${styles.recommendationItem} ${styles.recent}`}>
            <h3>제주도 숨은 맛집</h3>
            <p>현지인만 아는 흑돼지 맛집...</p>
        </div>
    );
};

export default RecentPinStory;