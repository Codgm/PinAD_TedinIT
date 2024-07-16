import React from 'react';
import styles from '@/app/styles/Recommendations.module.css';
import Image from 'next/image';
import SeoulNightViewImage from '/public/seoul-night-view.webp';
import JejuFoodImage from '/public/jeju-food-place.jpg';
const PopularPinStory = () => {
    return (
        <div className={styles.recommendationList}>
            <div className={styles.recommendationItem}>
                <div className={styles.image}>
                    <Image src={SeoulNightViewImage} alt="서울 야경 명소" layout="responsive" width={600} height={400} />
                </div>
                <div className={styles.text}>
                    <h3>서울 야경 명소</h3>
                    <p>남산에서 바라본 서울의 밤...</p>
                </div>
            </div>
            <div className={styles.recommendationItem}>
                <div className={styles.image}>
                    <Image src={JejuFoodImage} alt="부산 해운대 일출" layout="responsive" width={600} height={400} />
                </div>
                <div className={styles.text}>
                    <h3>부산 해운대 일출</h3>
                    <p>새해 첫날...</p>
                </div>
            </div>
            <div className={`${styles.recommendationItem}`}>
                <div className={styles.image}>
                    <Image src={JejuFoodImage} alt="제주도 숨은 맛집" layout="responsive" width={600} height={400} />
                </div>
                <div className={styles.text}>
                    <h3>제주도 숨은 맛집</h3>
                    <p>현지인만 아는 흑돼지 맛집...</p>
                </div>
            </div>
        </div>
    );
};

export default PopularPinStory;
