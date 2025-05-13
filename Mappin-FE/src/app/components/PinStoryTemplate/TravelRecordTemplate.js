import React from 'react';
import Styles from '@/app/styles/ReadTemplate.module.css';

const TravelRecord = () => {
  const travel = {
    name: '감귤밭',
    location: "제주도, 대한민국",
    memorableMoments: [
      "한라산 정상에서 바라본 일출은 말로 표현할 수 없는 장관이었습니다.",
      "제주도 바다의 파도 소리와 바람의 상쾌함이 마음을 평온하게 만들어주었습니다.",
      "감귤 농장에서 직접 따낸 신선한 감귤의 맛은 정말 특별했습니다."
    ],
    experiences: "제주도에서의 여행은 자연과 함께하는 시간이었으며, 한라산의 아름다운 풍경과 바다의 푸르름이 기억에 남습니다. 특히, 제주도 감귤 농장에서의 체험은 신선한 과일을 직접 따서 먹을 수 있는 특별한 경험이었습니다.",
    images: [
      "https://example.com/image1.jpg",
      "https://example.com/image2.jpg"
    ]
  };

  return (
    <div className={Styles.TravelRecordContainer}>
      {/* 사진 및 영상 섹션 */}
      <div className={Styles.TravelRecordHeader}>
        <h2 className={Styles.TravelRecordTitle}>{travel.name}</h2>
        <p className={Styles.TravelRecordLocation}>{travel.location}</p>
        <div className={Styles.PhotoGallery}>
          {travel.images.map((image, index) => (
            <img
              key={index}
              src={image}
              alt={`Travel photo ${index + 1}`}
            />
          ))}
        </div>
      </div>

      {/* 오감 및 마음에 새겨진 한컷들 섹션 */}
      <div>
        <h3 className={Styles.TravelRecordSubtitle}>오감과 의미있던 한컷들</h3>
        <ul className={Styles.MemorableMoments}>
          {travel.memorableMoments.map((moment, index) => (
            <li key={index}>
              {moment}
            </li>
          ))}
        </ul>
      </div>

      {/* 느낄 수 있는 경험 섹션 */}
      <div className={Styles.Experiences}>
        <h3 className={Styles.TravelRecordSubtitle}>느낄 수 있는 경험</h3>
        <p>{travel.experiences}</p>
      </div>
    </div>
  );
};

export default TravelRecord;
