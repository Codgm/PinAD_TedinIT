import React from 'react';

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
    contact: "문의처: 02-1234-5678, 이메일: info@summerfestival.com"
  };

  return (
    <div className="p-6 ">
      <div className="mb-4">
        <h2 className="text-xl font-semibold">{event.title}</h2>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">행사 종류</h3>
        <p>{event.type}</p>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">행사 일정</h3>
        <p>시작일: {event.schedule.start}</p>
        <p>종료일: {event.schedule.end}</p>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">행사 내용</h3>
        <p>{event.details}</p>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">행사 비용 및 문의처</h3>
        <p>비용: {event.cost}</p>
        <p>문의처: {event.contact}</p>
      </div>
    </div>
  );
};

export default EventNotificationTemplate;
