import React from 'react';
import Styles from '@/app/styles/ReadTemplate.module.css';

const AppointmentPlace = () => {
  const appointment = {
    place: "서울시 종로구 세종로 1",
    time: "2024-08-10 15:00",
    purpose: "중요한 비즈니스 회의",
    details: "서울시청 본관 5층 회의실에서 진행됩니다. 자세한 위치는 서울시청 안내 데스크에서 확인 가능합니다.",
    items: "명함, 회의 자료, 노트북"
  };

  return (
    <div className={Styles.AppointmentContainer}>
      {/* 장소 이름 및 시간 섹션 */}
      <div className={Styles.AppointmentSection}>
        <h2 className={Styles.SectionTitle}>장소 및 시간</h2>
        <p><strong>장소:</strong> {appointment.place}</p>
        <p><strong>시간:</strong> {appointment.time}</p>
      </div>

      {/* 목적 및 장소 세부 사항 섹션 */}
      <div className={Styles.AppointmentSection}>
        <h2 className={Styles.SectionTitle}>목적 및 장소 세부 사항</h2>
        <p><strong>목적:</strong> {appointment.purpose}</p>
        <p><strong>세부 사항:</strong> {appointment.details}</p>
      </div>

      {/* 준비물 섹션 */}
      <div className={Styles.AppointmentSection}>
        <h2 className={Styles.SectionTitle}>준비물</h2>
        <p>{appointment.items}</p>
      </div>
    </div>
  );
};

export default AppointmentPlace;
