import React from 'react';

const AppointmentPlace = () => {
  const appointment = {
    place: "서울시 종로구 세종로 1",
    time: "2024-08-10 15:00",
    purpose: "중요한 비즈니스 회의",
    details: "서울시청 본관 5층 회의실에서 진행됩니다. 자세한 위치는 서울시청 안내 데스크에서 확인 가능합니다.",
    items: "명함, 회의 자료, 노트북"
  };

  return (
    <div className="p-6">
      <div className="mb-4">
        <h2 className="text-xl font-semibold">약속 장소</h2>
        <h3 className="text-lg font-semibold">장소 및 시간</h3>
        <p>장소: {appointment.place}</p>
        <p>시간: {appointment.time}</p>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">목적 및 장소 세부 사항</h3>
        <p>목적: {appointment.purpose}</p>
        <p>세부 사항: {appointment.details}</p>
      </div>
      <div>
        <h3 className="text-lg font-semibold">준비물</h3>
        <p>{appointment.items}</p>
      </div>
    </div>
  );
};

export default AppointmentPlace;
