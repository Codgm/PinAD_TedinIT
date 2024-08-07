import React from 'react';

const TravelRecord = () => {
  const travel = {
    place: "제주도",
    memorableMoments: [
      "한라산 정상에서 바라본 일출은 말로 표현할 수 없는 장관이었습니다.",
      "제주도 바다의 파도 소리와 바람의 상쾌함이 마음을 평온하게 만들어주었습니다.",
      "감귤 농장에서 직접 따낸 신선한 감귤의 맛은 정말 특별했습니다."
    ],
    experiences: "제주도에서의 여행은 자연과 함께하는 시간이었으며, 한라산의 아름다운 풍경과 바다의 푸르름이 기억에 남습니다. 특히, 제주도 감귤 농장에서의 체험은 신선한 과일을 직접 따서 먹을 수 있는 특별한 경험이었습니다."
  };

  return (
    <div className="p-6">
      <div className="mb-4">
        <h2 className="text-xl font-semibold">여행 기록</h2>
        <h3 className="text-lg font-semibold">{travel.place}</h3>
      </div>
      <div className="mb-4">
        <h3 className="text-lg font-semibold">오감과 의미있던 한컷들</h3>
        <ul className="list-disc list-inside ml-4">
          {travel.memorableMoments.map((moment, index) => (
            <li key={index} className="mb-2">
              {moment}
            </li>
          ))}
        </ul>
      </div>
      <div>
        <h3 className="text-lg font-semibold">느낄 수 있는 경험</h3>
        <p>{travel.experiences}</p>
      </div>
    </div>
  );
};

export default TravelRecord;
