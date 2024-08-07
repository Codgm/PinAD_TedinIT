import React from 'react';

const AttractionRecommendation = () => {
  const attraction = {
    locationDetails: "부산광역시 해운대구 해운대해변로 200",
    recommendations: "해운대 해수욕장은 여름철에 특히 인기가 많은 명소로, 깨끗한 해변과 다양한 해양 스포츠를 즐길 수 있습니다.",
    activities: "해수욕, 서핑, 해변 산책, 다양한 해양 스포츠",
    tips: "주말에는 사람이 많으니 평일 방문을 추천하며, 해변가에 위치한 다양한 음식점에서 식사를 즐길 수 있습니다."
  };

  return (
    <div className="p-6 ">
      <div className="mb-4">
        <h2 className="text-xl font-semibold">명소 추천</h2>
        <h3 className="text-lg font-semibold">장소 세부사항</h3>
        <p>{attraction.locationDetails}</p>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">추천 사항</h3>
        <p>{attraction.recommendations}</p>
      </div>
      <div>
        <h3 className="text-lg font-semibold">할것 및 팁</h3>
        <p>{attraction.activities}</p>
        <p>팁: {attraction.tips}</p>
      </div>
    </div>
  );
};

export default AttractionRecommendation;
