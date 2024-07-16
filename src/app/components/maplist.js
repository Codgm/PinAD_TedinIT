import React from 'react';

const articles = [
  {
    id: 1,
    title: '건강한 노후를 위한 5가지 습관',
    excerpt: '나이가 들어감에 따라 건강 관리의 중요성은 더욱 커집니다. 이 기사에서는 건강한 노후를 위한 5가지 핵심 습관을 소개합니다. 규칙적인 운동, 균형 잡힌 식단, 충분한 수면, 사회적 활동 참여, 그리고 정기적인 건강 검진이 그 핵심입니다.',
    date: '2024년 7월 15일',
    category: '건강'
  },
  {
    id: 2,
    title: '디지털 시대의 시니어 생활: 스마트폰 활용법',
    excerpt: '스마트폰은 이제 일상생활의 필수품이 되었습니다. 이 글에서는 시니어들을 위한 유용한 스마트폰 앱과 기본적인 사용법을 알아봅니다. 가족과의 화상 통화, 건강 관리 앱, 뉴스 앱, 그리고 간편한 결제 방법 등 일상을 더욱 편리하게 만드는 기술을 소개합니다.',
    date: '2024년 7월 14일',
    category: '기술'
  },
  {
    id: 3,
    title: '은퇴 후 새로운 취미 찾기: 당신의 열정을 깨우세요',
    excerpt: '은퇴는 새로운 시작입니다. 이 기사에서는 은퇴 후 시도해볼 만한 다양한 취미 활동과 그 이점에 대해 소개합니다. 가드닝, 요리, 그림 그리기, 악기 연주, 여행 등 다양한 활동을 통해 새로운 즐거움을 발견하고 활기찬 노후 생활을 즐기는 방법을 알아봅니다.',
    date: '2024년 7월 13일',
    category: '라이프스타일'
  }
];

const MapList = () => {
  return (
    <div class="flex flex-col" style={{height: "75vh;", overflow:"auto;"}}>
      
      {/* 스크롤 가능한 리스트 영역 */}
      <div className="flex-1 overflow-y-auto">
        <div className="container mx-auto p-4">
          <h1 className="text-3xl font-bold mb-6 text-gray-800">최신 기사</h1>
          <div className="space-y-8">
            {articles.map((article) => (
              <article key={article.id} className="bg-white rounded-lg shadow-md">
                <div className="p-6">
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-sm font-medium text-blue-600">{article.category}</span>
                    <span className="text-sm text-gray-500">{article.date}</span>
                  </div>
                  <h2 className="text-2xl font-semibold mb-3 text-gray-800">{article.title}</h2>
                  <p className="text-gray-600 mb-4">{article.excerpt}</p>
                  <button className="text-blue-600 hover:text-blue-800 font-medium">
                    자세히 보기 →
                  </button>
                </div>
              </article>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default MapList;