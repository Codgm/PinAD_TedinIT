import React from 'react';

const Review = () => {
  const review = {
    locationDetails: "서울특별시 강남구 테헤란로 123",
    rating: 4.5,
    features: "위치는 중심가에 위치해 있으며 교통이 매우 편리합니다. 또한, 다양한 음식점과 쇼핑몰이 가까워 방문하기에 좋습니다.",
    recommendation: "쇼핑과 미식 여행을 좋아하는 분들에게 추천합니다.",
    pros: "교통 편리, 다양한 음식점, 좋은 분위기",
    cons: "주차 공간 부족",
    comments: "전체적으로 매우 만족스러웠으나, 주차 공간이 부족해 조금 불편했습니다."
  };

  return (
    <div className="p-6">
      <div className="mb-4">
        <h2 className="text-xl font-semibold">리뷰</h2>
        <h3 className="text-lg font-semibold">장소 세부사항</h3>
        <p>{review.locationDetails}</p>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">평점</h3>
        <p>{review.rating} / 5</p>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">특징과 추천 대상</h3>
        <p>{review.features}</p>
        <p>{review.recommendation}</p>
      </div>
      <div>
        <h3 className="text-lg font-semibold">장단점과 추가 코멘트</h3>
        <p>장점: {review.pros}</p>
        <p>단점: {review.cons}</p>
        <p>추가 코멘트: {review.comments}</p>
      </div>
    </div>
  );
};

export default Review;
