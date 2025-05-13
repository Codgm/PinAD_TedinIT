'use client'
import { useState } from "react";

const defaultTags = {
    '리뷰': [
      '#솔직리뷰', 
      '#사용자후기', 
      '#제품리뷰', 
      '#서비스평가',  
      '#고객리뷰', 
      '#제품평가',  
      '#리뷰꿀팁',
      '#장단점',
      '#구매팁',
      '#사용법'
    ],
    '명소추천': [
      '#명소추천', 
      '#여행명소', 
      '#핫플레이스', 
      '#숨겨진명소', 
      '#여행필수장소', 
      '#도시여행', 
      '#추천장소', 
      '#해변여행', 
      '#인생샷', 
      '#인스타명소'
    ],
    '약속장소': [
      '#미팅장소', 
      '#데이트장소', 
      '#모임장소', 
      '#카페추천', 
      '#식사장소', 
      '#회의장소', 
      '#편안한장소', 
      '#프라이빗룸', 
      '#좋은환경'
    ],
    '여행기록': [
      '#여행기록', 
      '#트래블로그', 
      '#여행메모', 
      '#여행스케치', 
      '#여행추억', 
      '#여행경험', 
      '#여행일정',
      '#여행하고싶다',
      '#여행의모듯것', 
      '#여행후기'
    ],
    '유통': ['#한정특가', '#오늘단하루', '#점심타임딜', '#막차세일', '#반값찬스', '#골든타임특가', '#긴급할인', '#번개세일', '#타임어택','#초특가_1시간', '#지금이기회', '#순간최저가', '#마감임박할인', '#깜짝특가', '#득템찬스'],
    '요식업' :[ "#맛집", "#데이트", "#미슐랭", "#오마카세", "#가성비", "#브런치", "#디저트", "#커피스타그램", "#카페투어", "#인스타감성", "#술스타그램", "#칵테일", "#혼술", "#안주", "#펍", "#혼밥", "#친구모임", "#뷰맛집", "#숨은맛집", "#야식"],
    '행사알림' :[ "#나눔과소통", "#변화를위해", "#지역사회행사", "#사회적책임", "#컨퍼런스", "#페스티벌", "#특별게스트","#트렌드"],
  };
export default function Tagslistup({selectedCategory}){
    const [newTags, setNewTags] = useState([]);
    const [selectedDefaultTags, setSelectedDefaultTags] = useState([]);

    const handleDefaultTagClick = (tag) => {
        setSelectedDefaultTags(prev => {
          if (prev.includes(tag)) {
            return prev.filter(t => t !== tag);
          } 
          else if (prev.length < 5) {
            return [...prev, tag];
          } 
          else {
            return prev;
          }
        });
      };
      
    
      const handleNewTagInputKeyDown = (e) => {
        if (e.key === 'Enter' && e.target.value.trim()) {
          const newTag = e.target.value.trim();
          if (!newTags.includes(newTag) && !selectedDefaultTags.includes(newTag) && (newTags.length + selectedDefaultTags.length) < 10) {
            setNewTags(prev => [...prev, newTag]);
            e.target.value = '';
          }
        }
      };
    
      const removeNewTag = (tag) => {
        setNewTags(prev => prev.filter(t => t !== tag));
      };

  return (
    <div className="space-y-4">
    <div className="space-y-2">
      <label className="block mb-2 text-sm text-gray-600">기본 태그 (최대 5개), 입력 태그(최대 10개)</label>
      <div className="flex flex-wrap">
      {Object.keys(defaultTags).includes(selectedCategory) && (defaultTags[selectedCategory].map(tag => (
        <button
        key={tag}
        className={`p-2 m-1 rounded-md ${selectedDefaultTags.includes(tag) ? 'bg-purple-500 text-white' : 'bg-gray-200 text-gray-700'} focus:outline-none`}
        onClick={() => handleDefaultTagClick(tag)}
        >
        {tag}
        </button>
      )))}
      </div>
      <div className="flex flex-wrap ">
      {newTags.map(tag => (
        <div key={tag} className="flex items-center bg-purple-500 text-white p-2 m-1 rounded-md">
          <span>{tag}</span>
          <button onClick={() => removeNewTag(tag)} className="ml-2 text-xs">×</button>
        </div>
      ))}
      </div>
      <input
      type="text"
      className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 mt-2"
      placeholder="새 태그를 입력하고 Enter 키를 누르세요"
      onKeyDown={handleNewTagInputKeyDown}
      />
    </div>
  </div> 
  )
}

