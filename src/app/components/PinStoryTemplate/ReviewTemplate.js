import React from 'react';
import Styles from '@/app/styles/ReadTemplate.module.css';

const Review = () => {
  const review = {
    placeName: "서울 한화 리조트",
    location: "서울특별시 강남구 테헤란로 123",
    rating: 4.5,
    features: "위치는 중심가에 위치해 있으며 교통이 매우 편리합니다. 또한, 다양한 음식점과 쇼핑몰이 가까워 방문하기에 좋습니다.",
    recommendation: "쇼핑과 미식 여행을 좋아하는 분들에게 추천합니다.",
    pros: "교통 편리, 다양한 음식점, 좋은 분위기",
    cons: "주차 공간 부족",
    comments: "전체적으로 매우 만족스러웠으나, 주차 공간이 부족해 조금 불편했습니다."
  };

  return (
    <div className={Styles.ReviewContainer}>
      {/* 업소 이름 섹션 */}
      <div className={`${Styles.ReviewSection} ${Styles.PlaceNameSection}`}>
        <h1 className={Styles.PlaceName}>{review.placeName}</h1>
        <h2 className={Styles.ReviewLocation}>{review.location}</h2>
      </div>

      {/* 평점 섹션 */}
      <div className={`${Styles.ReviewSection} ${Styles.ReviewRatingSection}`}>
        <h2 className={Styles.ReviewTitle}>평점</h2>
        <p className={Styles.ReviewRating}>{review.rating} / 5</p>
      </div>

      {/* 장단점 섹션 */}
      <div className={`${Styles.ReviewSection} ${Styles.ProsConsSection}`}>
        <h3 className={Styles.ReviewSubtitle}>장단점</h3>
        <p><strong>장점:</strong> {review.pros}</p>
        <p><strong>단점:</strong> {review.cons}</p>
      </div>

      {/* 특징 및 추천 섹션 */}
      <div className={`${Styles.ReviewSection} ${Styles.FeaturesRecommendationSection}`}>
        <h3 className={Styles.ReviewSubtitle}>특징과 추천 대상</h3>
        <p>{review.features}</p>
        <p><strong>추천 대상:</strong> {review.recommendation}</p>
      </div>

      {/* 장소 세부사항 및 추가 코멘트 섹션 */}
      <div className={`${Styles.ReviewSection} ${Styles.AdditionalCommentsSection}`}>
        <h3 className={Styles.ReviewSubtitle}>장소 세부사항</h3>
        <p>{review.locationDetails}</p>
        <h3 className={Styles.ReviewSubtitle}>추가 코멘트</h3>
        <p>{review.comments}</p>
      </div>
    </div>
  );
};

export default Review;
