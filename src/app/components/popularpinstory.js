import React from 'react';
import styles from '@/app/styles/Recommendations.module.css';

const PopularPinStory = () => {
    return (
        <div className={`${styles.recommendationItem} ${styles.popular}`}>
            <h3>서울 야경 명소</h3>
            <p>남산에서 바라본 서울의 밤...</p>
        </div>
    );
};

export default PopularPinStory;