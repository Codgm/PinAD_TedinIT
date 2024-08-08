import React from 'react';
import Styles from '@/app/styles/ReadTemplate.module.css';

const EventNotificationTemplate = () => {
  const event = {
    title: "여름 축제 2024",
    type: "축제",
    schedule: {
      start: "2024-08-15",
      end: "2024-08-17"
    },
    details: "여름 축제 2024는 다양한 문화 행사와 음악 공연, 음식 부스가 마련된 대규모 축제입니다. 가족과 친구들과 함께 즐거운 시간을 보내세요!",
    cost: "입장 무료, 일부 액티비티 및 음식은 유료",
    contact: "문의처: 02-1234-5678, 이메일: info@summerfestival.com",
    images: [
      "https://example.com/festival1.jpg",
      "https://example.com/festival2.jpg"
    ]
  };

  return (
    <div className={Styles.EventNotificationContainer}>
      {/* 제목 섹션 */}
      <div className={Styles.EventTitleSection}>
        <h1 className={Styles.EventTitle}>{event.title}</h1>
      </div>

      {/* 행사 종류 섹션 */}
      <div className={Styles.EventDetailsSection}>
        <h2 className={Styles.EventSubtitle}>행사 종류</h2>
        <p className={Styles.EventType}>{event.type}</p>
      </div>

      {/* 행사 일정 섹션 */}
      <div className={Styles.EventDetailsSection}>
        <h2 className={Styles.EventSubtitle}>행사 일정</h2>
        <p>시작일: {event.schedule.start}</p>
        <p>종료일: {event.schedule.end}</p>
      </div>

      {/* 이미지 섹션 */}
      <div className={Styles.EventImagesSection}>
        <h2 className={Styles.EventSubtitle}>행사 사진</h2>
        <div className={Styles.ImageGallery}>
          {event.images.map((image, index) => (
            <img key={index} src={image} alt={`Event image ${index + 1}`} className={Styles.EventImage} />
          ))}
        </div>
      </div>

      {/* 행사 내용 섹션 */}
      <div className={Styles.EventDetailsSection}>
        <h2 className={Styles.EventSubtitle}>행사 내용</h2>
        <p>{event.details}</p>
      </div>

      {/* 행사 비용 및 문의처 섹션 */}
      <div className={Styles.EventCostContactSection}>
        <h2 className={Styles.EventSubtitle}>행사 비용 및 문의처</h2>
        <p>비용: {event.cost}</p>
        <p>문의처: {event.contact}</p>
      </div>
    </div>
  );
};

export default EventNotificationTemplate;
