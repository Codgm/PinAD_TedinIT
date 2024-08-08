import React from 'react';
import Styles from '@/app/styles/ReadTemplate.module.css'; 

const AttractionRecommendation = () => {
  const attraction = {
    name: "해운대 해수욕장",
    locationDetails: "부산광역시 해운대구 해운대해변로 200",
    recommendations: "해운대 해수욕장은 여름철에 특히 인기가 많은 명소로, 깨끗한 해변과 다양한 해양 스포츠를 즐길 수 있습니다.",
    activities: "해수욕, 서핑, 해변 산책, 다양한 해양 스포츠",
    tips: "주말에는 사람이 많으니 평일 방문을 추천하며, 해변가에 위치한 다양한 음식점에서 식사를 즐길 수 있습니다."
  };

  return (
    <div className={Styles.AttractionContainer}>
      {/* 명소 이름 섹션 */}
      <div className={Styles.AttractionHeader}>
        <h1 className={Styles.AttractionName}>{attraction.name}</h1>
      </div>

      {/* 장소 세부사항 섹션 */}
      <div className={Styles.AttractionSection}>
        <h2 className={Styles.SectionTitle}>장소 세부사항</h2>
        <p>{attraction.locationDetails}</p>
      </div>

      {/* 추천 사항 섹션 */}
      <div className={Styles.AttractionSection}>
        <h2 className={Styles.SectionTitle}>추천 사항</h2>
        <p>{attraction.recommendations}</p>
      </div>

      {/* 할 것 및 팁 섹션 */}
      <div className={Styles.AttractionSection}>
        <h2 className={Styles.SectionTitle}>할 것 및 팁</h2>
        <p>{attraction.activities}</p>
        <p><strong>팁:</strong> {attraction.tips}</p>
      </div>
    </div>
  );
};

export default AttractionRecommendation;
