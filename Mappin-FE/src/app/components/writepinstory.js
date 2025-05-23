"use client";
import React, { useState, useRef, useEffect } from 'react';
import { FaCamera, FaPen, FaTags, FaShare, FaCheck, FaImage, FaVideo } from 'react-icons/fa';
import { MdKeyboardArrowRight } from "react-icons/md";
import usePostCreation from '@/app/components/logic/writeSeq';
import AdStepComponent from './AdStepComponent';
import Styles from '@/app/styles/writepinstory.module.css';
import EventNotification from './eventnotifcomponent';
import Review from './reviewcomponent';
import Attraction from './attractioncomponent';
import TravelmemoComponent from './travelmemoComponent';
import AppointmentForm from './AppointmentComponent';
import Tagslistup from './tagslistup';

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
  
};


const steps = [
  { icon: <FaCamera />, label: '미디어' },
  { icon: <FaPen />, label: '내용' },
  { icon: <FaTags />, label: '관심사' },
  { icon: <FaCheck />, label: '설정 및 실행' },
];

export default function WritePinStory({ isOpen, closeModal, addPin }) {
  const {
    state,
    setStep,
    setTitle,
    setDiscountType,
    setDiscountValue,
    setBundleDiscountType,
    setBundleDiscountValue,
    setType,
    setContent,
    addImage,
    setShortVideo,
    clearErrors,
    setPlatforms,
    setProfilePublic,
    setPlatformLink,
    togglePlatformSelection,
    setMonetize,
    setAdOption,
    setBoost,
    setStartDate,
    setEndDate,
    setStock,
    setRadius,
    setLocation,
    setPayment,
    setSelectedCategory,
    setBundleNValue,
    setBundleType,
    setPaymentMode
  } = usePostCreation();

  const [selectedDefaultTags, setSelectedDefaultTags] = useState([]);
  const [newTags, setNewTags] = useState([]);
  const [showMonetizeModal, setShowMonetizeModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [industryType, setIndustryType] = useState('');
  const [showAdMonetizeModal, setshowAdMonetizeModal] = useState(false);
  const [isSpinning, setIsSpinning] = useState(false);
  const [category, setCategory] = useState(true);

  const fileInputRef = useRef(null);
  const dropdownRef = useRef(null);
  
  useEffect(() => {
    console.log('Selected Category:', state.selectedCategory);
    console.log('Type:', state.type);
    if (state.selectedCategory !== '' && state.type !== '') {
      // 2초 후에 showDropdown을 false로 설정
      const timer = setTimeout(() => {
        setCategory(false);
      }, 10);

      // 컴포넌트 언마운트 시 타이머 클리어
      return () => clearTimeout(timer);
    } else {
      setCategory(true); // 선택된 카테고리나 타입이 null일 때는 dropdown을 표시
    }
  }, [state.selectedCategory, state.type]);

  const handleTitleChange = (e) => setTitle(e.target.value);
  const handleTypeChange = (e) => setType(e.target.value);
  const handleContentChange = (e) => setContent(e.target.value);
  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    files.forEach(file => addImage(file));
  };

  const handleShortVideoChange = (e) => {
    const file = e.target.files[0];
    if (file && file.size <= 50 * 1024 * 1024) {
      setShortVideo(file);
    } else {
      alert('비디오 파일 크기는 50MB 이하로 업로드 해주세요.');
    }
  };

  const handlePost = () => {
    if (state.type === '광고') {
      setshowAdMonetizeModal(true); 
    } else if (state.type !== '광고' && !showMonetizeModal) {
      setShowMonetizeModal(true);
    } else {
      const fileUrls = state.imageFiles.map(file => URL.createObjectURL(file));
      addPin({images :fileUrls,isUser: false,type:state.type,category:state.selectedCategory,title:state.title,location:state.location,details:state.details});
      closeModal();
    }
  };

  const handleOutsideClick = (e) => {
    if (e.target.classList.contains('modal-overlay')) {
      closeModal();
    }
  };

  const handlePlatformLinkChange = (e, platform) => {
    const value = e.target.value;
    setPlatformLink(platform, value);
  };

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

  const handleFileUpload = (e) => {
    const files = Array.from(e.target.files);
    files.forEach(file => {
      if (file.type.startsWith('image/')) {
        if (state.imageFiles.length < 4) {
          addImage(file);
        }
      } else if (file.type.startsWith('video/')) {
        if (file.size <= 50 * 1024 * 1024) {
          setShortVideo(file);
        } else {
          alert('비디오 파일 크기는 50MB 이하로 업로드 해주세요.');
        }
      }
    });
  };

  const handleMonetizeChoice = (choice) => {
    if (choice) {
      console.log(state.selectedCategory);
      console.log(state.type);
      console.log(state.title);
      console.log(state.location);
      console.log(state.details);
      const confirmation = window.confirm(`${state.selectedCategory} 광고를 실행하고 핀을 꽂을까요?`);
      if (confirmation) {
        setMonetize(false);
        const fileUrls = state.imageFiles.map(file => URL.createObjectURL(file));
        addPin({images :fileUrls,isUser: false,type:state.type,category:state.selectedCategory,title:state.title,location:state.location,details:state.details});

        closeModal();
      }else{
        setShowConfirmModal(false);
        setshowAdMonetizeModal(false);
        setMonetize(false);
      }
    } else {
      setShowConfirmModal(true);
      setshowAdMonetizeModal(true);
    }
  };

  const handleConfirmChoice = (choice) => {
    if (choice) {
      const confirmation = window.confirm(`${state.selectedCategory} 광고를 실행하고 핀을 꽂을까요?`);
      if (confirmation) {
        setMonetize(false);
        const fileUrls = state.imageFiles.map(file => URL.createObjectURL(file));
        addPin({images :fileUrls,isUser: false,type:state.type,category:state.selectedCategory,title:state.title,location:state.location,details:state.details});
        closeModal();
      }else{
        setShowConfirmModal(false);
        setshowAdMonetizeModal(false);
        setMonetize(false);
      }
    } else {
      setShowConfirmModal(false);
      setShowMonetizeModal(true);
    }
  };

  const handleStep = (nextStep) => {
    setStep(nextStep);
  };

  const handleCategoryChange = (e) => {
    setSelectedCategory(e.target.value);
    setIndustryType('');
  };

  const handleAdOptionChange = (event) => {
    setAdOption(event.target.value);
  };

  const handlePaymentModeChange = (e) => {
    setPaymentMode((prevState) => ({
      ...prevState,
      paymentMode: e.target.value,
    }));
  };

  const [wallpaper, setWallpaper] = useState("");

  const wallpaperStyles = {
    "유통": {
      backgroundColor: '#f4f7f6', /* 기본 배경 색상 (연한 회색-녹색) */
      backgroundImage: `
          linear-gradient(
              180deg, /* 상단에서 하단으로 그라데이션 */
              rgba(244, 247, 246, 1) 0%, /* 상단 부분은 흰색에 가까운 연한 색상 */
              rgba(221, 232, 226, 1) 50%, /* 중간 부분은 서브 텍스트 색상과 비슷한 색상으로 전환 */
              rgba(138, 112, 254, 0.8) 95%, /* 하단 부분은 제목 색상으로 전환 */
              rgba(255, 111, 97, 0.7) 100% /* 하단 끝부분에는 약간의 붉은 계열 강조 */
          )
      `,
      backgroundSize: '100% 100%', /* 배경 크기 설정 */
      backgroundPosition: '0 0', /* 위치 설정 */
      backgroundRepeat: 'no-repeat' /* 반복 설정 */
    },
    "행사알림": {
      backgroundColor: '#f6fefd', /* 기본 배경 색상 */
      backgroundImage: `
        linear-gradient(
          180deg, /* 상단에서 하단으로 그라데이션 */
          rgba(255, 255, 255, 0.9) 0%, /* 상단 부분을 아주 밝게 */
          #f6fefd 40%, /* 중간 부분은 기본 색상 */
          #e0e5e9 70%, /* 하단 부분은 조금 더 짙게 */
          #113598 100% /* 가장 하단은 짙은 파란색 */
        )
      `,
      backgroundSize: '100% 100%', /* 전체 배경에 맞춤 */
      backgroundPosition: '0 0', /* 위치 설정 */
      backgroundRepeat: 'no-repeat', /* 배경 반복 비활성화 */
    },
    "부동산": {
      backgroundColor: '#B22222', /* 기본 붉은색 배경 */
      backgroundImage: `
        linear-gradient(0deg, rgba(255, 255, 255, 0.2) 1px, transparent 1px),
        linear-gradient(90deg, rgba(255, 255, 255, 0.2) 1px, transparent 1px),
        radial-gradient(circle, rgba(0, 0, 0, 0.2) 25%, transparent 50%)
      `,
      backgroundSize: '40px 40px, 40px 40px, 100px 100px', /* 각 패턴의 크기 설정 */
      backgroundPosition: '0 0, 0 0, 0 0', /* 각 패턴의 위치 설정 */
      backgroundRepeat: 'repeat', /* 패턴 반복 설정 */
    },
    "F&B": {
      backgroundColor: '#fefaf2', /* 기본 배경 색상 (연한 크림색) */
      backgroundImage: `
        linear-gradient(
          180deg, /* 상단에서 하단으로 그라데이션 */
          rgba(255, 245, 225, 0.8) 0%, /* 상단 부분을 밝고 부드럽게 */
          rgba(255, 245, 225, 0.5) 50%, /* 중간 부분은 기본 색상으로 */
          rgba(255, 245, 225, 0.3) 100% /* 하단 부분은 좀 더 투명하게 */
        ),
        radial-gradient(
          circle at top left, /* 원형 그라데이션 추가 */
          rgba(255, 245, 225, 0.3) 0%, 
          rgba(255, 245, 225, 0.2) 50%, 
          rgba(255, 245, 225, 0.1) 100%
        ),
        repeating-linear-gradient(
          45deg,
          rgba(0, 0, 0, 0.05) 0px, 
          rgba(0, 0, 0, 0.05) 10px, 
          rgba(0, 0, 0, 0.02) 10px, 
          rgba(0, 0, 0, 0.02) 20px
        )
      `,
      backgroundSize: '100% 100%, 200px 200px, 30px 30px', /* 각 배경의 크기 설정 */
      backgroundPosition: '0 0, 0 0, 0 0', /* 위치 설정 */
      backgroundRepeat: 'no-repeat, no-repeat, repeat', /* 반복 설정 */
    },
    "구인": {
      backgroundColor: '#4CAF50',
      backgroundImage: 'linear-gradient(135deg, #45a247 25%, transparent 25%), linear-gradient(225deg, #45a247 25%, transparent 25%), linear-gradient(45deg, #45a247 25%, transparent 25%), linear-gradient(315deg, #45a247 25%, #4CAF50 25%)',
      backgroundPosition: '10px 0, 10px 0, 0 0, 0 0',
      backgroundSize: '20px 20px',
      backgroundRepeat: 'repeat'
    },
    "리뷰": {
      backgroundColor: '#f2f7f9', /* 기본 배경 색상 */
      backgroundImage: `
          linear-gradient(
              180deg, /* 상단에서 하단으로 그라데이션 */
              rgba(242, 247, 249, 0.9) 0%, /* 상단 부분을 더 밝게 */
              rgba(242, 247, 249, 0.7) 50%, /* 중간 부분은 기본 색상으로 */
              rgba(242, 247, 249, 0.5) 100% /* 하단 부분은 더 투명하게 */
          ),
          radial-gradient(
              circle at center, /* 중심에서 방사형 그라데이션 */
              rgba(41, 128, 185, 0.2) 0%, /* 중앙 부분을 연한 파란색으로 */
              rgba(41, 128, 185, 0.1) 70%, /* 외곽 부분은 더 투명하게 */
              transparent 100%
          ),
          radial-gradient(
              circle at top right, /* 별 모양 패턴 */
              rgba(255, 221, 51, 0.6) 0%, /* 작은 별 모양을 강조 */
              rgba(255, 221, 51, 0.4) 50%, 
              transparent 100%
          )
      `,
      backgroundSize: '100% 100%, 150px 150px, 60px 60px', /* 각 배경의 크기 설정 */
      backgroundPosition: '0 0, 50% 50%, 0 0', /* 위치 설정 */
      backgroundRepeat: 'no-repeat, no-repeat, repeat' /* 반복 설정 */
    },
    "명소추천": {
      backgroundColor: '#f4f7f6', /* 기본 배경 색상 (하양) */
      backgroundImage: `
          linear-gradient(
              180deg, /* 상단에서 하단으로 그라데이션 */
              rgba(255, 255, 255, 0.9) 50%, /* 상단 부분은 거의 하양 */
              rgba(201, 236, 188, 0.8) 70%, /* 중간 부분에서 연한 녹색으로 전환 */
              rgba(178, 224, 167, 0.6) 100% /* 하단 부분은 연한 녹색으로 강조 */
          )
      `,
      backgroundSize: '100% 100%', /* 배경 크기 설정 */
      backgroundPosition: '0 0', /* 위치 설정 */
      backgroundRepeat: 'no-repeat' /* 반복 설정 */
    },
    "약속장소": {
      backgroundColor: '#f4f4f4', /* 기본 배경 색상 */
      backgroundImage: `
          linear-gradient(
              135deg, /* 대각선 그라데이션 방향 */
              rgba(0, 102, 204, 0.3) 0%, /* 강조된 연한 파스텔 블루 */
              rgba(0, 102, 204, 0.3) 50%, /* 강조된 연한 파스텔 블루 */
              rgba(102, 153, 255, 0.3) 50%, /* 강조된 연한 파스텔 스카이 블루 */
              rgba(102, 153, 255, 0.3) 100% /* 강조된 연한 파스텔 스카이 블루 */
          ),
          radial-gradient(
              circle at top left, /* 원형 그라데이션 */
              rgba(0, 102, 204, 0.2) 0%, /* 강조된 연한 파스텔 블루 */
              rgba(0, 102, 204, 0.1) 50%, 
              transparent 100%
          )
      `,
      backgroundSize: '200px 200px, 200px 200px', /* 배경 크기 설정 */
      backgroundPosition: '0 0, 0 0', /* 위치 설정 */
      backgroundRepeat: 'no-repeat, no-repeat' /* 반복 설정 */
    },
    "여행기록": {
      backgroundColor: '#f5f3f8', /* 기본 배경 색상 (하양) */
      backgroundImage: `
          linear-gradient(
              180deg, /* 상단에서 하단으로 그라데이션 */
              rgba(245, 243, 248, 0.9) 40%, /* 상단 부분은 거의 하양 */
              rgba(227, 207, 250, 0.6) 70%, /* 중간 부분은 연한 보라색으로 전환 */
              rgba(107, 33, 168, 0.4) 100% /* 하단 부분은 보라색으로 강조 */
          )
      `,
      backgroundSize: '100% 100%', /* 배경 크기 설정 */
      backgroundPosition: '0 0', /* 위치 설정 */
      backgroundRepeat: 'no-repeat' /* 반복 설정 */
    },
  };

  const handleWallpaperClick = () => {
    setType(''); // 타입을 빈 문자열로 설정
    setSelectedCategory('')
    setStep(2);
  };
  
  useEffect(() => {
    setWallpaper("");
  }, [state.type]);

  const renderStepIndicator = () => (
    <div className="flex mb-6 justify-around items-center overflow-y-auto">
      <div className='flex flex-rows'>
      {steps.map((stepItem, index) => (
        <div key={index} className={`flex flex-col items-center mx-2 ${index === state.step - 1 ? 'text-purple-500' : 'text-gray-400'}`}>
          <div className="flex flex-row items-center">
            <div className={`rounded-full py-2 px-1 ${index === state.step - 1 ? 'purple-100' : 'gray-100'}`}>
              {stepItem.icon}
            </div>
            {index !== steps.length - 1 && (
              <div className={`rounded-full py-2 px-1 ${index === state.step - 1 ? 'purple-100' : 'gray-100'}`}>
                {<MdKeyboardArrowRight />}
              </div>
            )}
          </div>
          <span className="text-xs mt-1">{stepItem.label}</span>
        </div>
      ))}
      </div>
      {
        wallpaper =="" ? <span className="text-l  p-2 text-black font-bold"/> :<span className="text-l  p-2 rounded-md text-black font-bold shadow-md" style={{...wallpaperStyles[wallpaper]}} onClick={handleWallpaperClick}><span>{state.type}/</span><span>{wallpaper}</span></span>
      }    
    </div>
  );

  const renderStep = () => {
    
    switch (state.step) {
      case 1:
        return (
          <div className="px-4 md:px-6 space-y-4 overflow-x-hidden">
            <div className="grid grid-cols-2 gap-4 h-60">
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 flex flex-col items-center justify-center cursor-pointer hover:bg-gray-50 relative overflow-hidden">
                {state.imageFiles.length > 0 ? (
                  <div className="absolute inset-0 flex items-center justify-center">
                    <img src={URL.createObjectURL(state.imageFiles[0])} alt="Uploaded" className="w-full h-full object-cover" />
                  </div>
                ) : (
                  <>
                    <FaImage className="text-3xl text-gray-400 mb-2" />
                    <span className="text-sm text-gray-600">사진 촬영</span>
                  </>
                )}
                <input
                  type="file"
                  multiple
                  accept="image/*"
                  className="absolute inset-0 opacity-0 cursor-pointer"
                  onChange={handleImageChange}
                />
              </div>
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 flex flex-col items-center justify-center cursor-pointer hover:bg-gray-50 relative overflow-hidden">
                {state.shortVideo ? (
                  <div className="absolute inset-0 flex items-center justify-center">
                    <video src={URL.createObjectURL(state.shortVideo)} className="w-full h-full object-cover" />
                  </div>
                ) : (
                  <>
                    <FaVideo className="text-3xl text-gray-400 mb-2" />
                    <span className="text-sm text-gray-600">영상 촬영</span>
                  </>
                )}
                <input
                  type="file"
                  accept="video/*"
                  className="absolute inset-0 opacity-0 cursor-pointer"
                  onChange={handleShortVideoChange}
                />
              </div>
            </div>
            <input
              type="file"
              multiple
              accept="image/*,video/*"
              className="hidden"
              ref={fileInputRef}
              onChange={handleFileUpload}
            />
            <div className='py-3'>
              <button
                className="w-full py-2 bg-gray-200 text-gray-600 rounded-lg hover:bg-gray-300 transition-colors duration-300"
                onClick={() => fileInputRef.current.click()}
              >
                기존 미디어 추가
              </button>
            </div>
          </div>
        );
      case 2:
        return (
          <div className="space-y-2">
            {category && <div className="flex items-center w-full space-x-2">
              <div className={Styles.buttonContainer}>
                <button
                  className={`${Styles.toggleButton1} ${state.active ? 'active' : ''}`}
                  onClick={() => { setType('광고');}}
                  onChange={handleTypeChange}
                >
                  광고
                </button>
                <button
                  className={`${Styles.toggleButton2} ${state.active ? 'active' : ''}`}
                  onClick={() => { setType('핀스토리'); }}
                  onChange={handleTypeChange}
                >
                  핀스토리
                </button>
              </div>
            </div>}
            <div className={Styles.dropdownMenu}>
            {state.type === '광고' && (
              <select
                className={Styles.dropdownOptions}
                value={state.selectedCategory}
                onChange={(e) => {
                  setSelectedCategory(e.target.value);
                  setWallpaper(e.target.value);
                }}
                ref={dropdownRef}
              >
                <option value="">선택</option>
                <option value="유통">유통</option>
                <option value="F&B">F&B</option>
                <option value="행사알림">행사알림</option>
                <option value="부동산">부동산</option>
                <option value="구인">구인</option>
              </select>
            )}
            {state.type === '핀스토리' && (
              <select
                className={Styles.dropdownOptions}
                value={state.selectedCategory}
                onChange={(e) => {
                  setSelectedCategory(e.target.value);
                  setWallpaper(e.target.value);
                }}
                ref={dropdownRef}
              >
                <option value="">선택</option>
                <option value="리뷰">리뷰</option>
                <option value="명소추천">명소추천</option>
                <option value="약속장소">약속장소</option>
                <option value="여행기록">여행기록</option>
              </select>
              )}
            </div>
            {
              state.selectedCategory === '행사알림'&& state.type === '광고' &&(
                <EventNotification/>
              )
            }
            {state.selectedCategory === '여행기록' && state.type === '핀스토리' &&
            (
              <TravelmemoComponent/>
            )}
            {state.selectedCategory === '약속장소' && state.type === '핀스토리' &&
            (
              <AppointmentForm/>
            )}
            {state.selectedCategory === '리뷰' && state.type === '핀스토리' && (
              <Review/>
            )}
            {state.selectedCategory === '명소추천' && state.type === '핀스토리' && (
              <Attraction/>
            )}
            { <AdStepComponent
                state={state}
                radius={state.radius}
                setStep={setStep}
                setContent={setContent}
                handleCategoryChange={handleCategoryChange}
                setSelectedCategory={setSelectedCategory}
                setIndustryType={setIndustryType}
                setStock={setStock}
                setDiscountType={setDiscountType}
                setDiscountValue={setDiscountValue}
                setBundleType={setBundleType}
                setBundleDiscountValue={setBundleDiscountValue}
                setBundleNValue = {setBundleNValue}
                handleAdOptionChange={handleAdOptionChange}
                setRadius={setRadius}
                setBoost={setBoost}
                setStartDate={setStartDate}
                setEndDate={setEndDate}
                setLocation={setLocation}
                setPayment={setPayment}
                handleDefaultTagClick={handleDefaultTagClick}
                handleNewTagInputKeyDown={handleNewTagInputKeyDown}
                removeNewTag={removeNewTag}
                newTags={newTags}
                selectedDefaultTags={selectedDefaultTags}
                handlePlatformLinkChange={handlePlatformLinkChange}
                handlePaymentModeChange = {handlePaymentModeChange}
              />
            }
          </div>
        );
      case 3:
        return (
          <Tagslistup selectedCategory={state.selectedCategory}/>
        );
      case 4:
        return (
          <AdStepComponent
            state={state}
            setStep={setStep}
            handleCategoryChange={handleCategoryChange}
            setSelectedCategory={setSelectedCategory}
            setIndustryType={setIndustryType}
            setStock={setStock}
            setDiscountType={setDiscountType}
            setDiscountValue={setDiscountValue}
            setBundleType={setBundleType}
            setBundleNValue = {setBundleNValue}
            setBundleDiscountValue={setBundleDiscountValue}
            handleAdOptionChange={handleAdOptionChange}
            setRadius={setRadius}
            setBoost={setBoost}
            setStartDate={setStartDate}
            setEndDate={setEndDate}
            setLocation={setLocation}
            setPayment={setPayment}
            handleDefaultTagClick={handleDefaultTagClick}
            handleNewTagInputKeyDown={handleNewTagInputKeyDown}
            removeNewTag={removeNewTag}
            newTags={newTags}
            selectedDefaultTags={selectedDefaultTags}
            handlePlatformLinkChange={handlePlatformLinkChange}
            handlePaymentModeChange = {handlePaymentModeChange}
          />
        );
      default:
        return null;
    }
  };

  const renderStepButtons = () => {
    return (
      <div className="m-4 flex justify-between">
        <button
          onClick={() => setStep(Math.max(1, state.step - 1))}
          className={`px-4 py-2 text-gray-600 rounded ${state.step === 1 ? 'bg-gray-100 cursor-not-allowed' : 'bg-gray-200 hover:bg-gray-300'}`}
          disabled={state.step === 1}
        >
          이전
        </button>
        <button
          onClick={() => {
            // if (state.step === 3 && state.type === '광고') {
            //   handleNextStep();
            // }
             {
              state.step < 4 ? setStep(state.step + 1) : handlePost();
            }
          }}
          className="px-4 py-2 bg-purple-500 text-white rounded hover:bg-purple-600 transition-colors duration-300"
        >
          {state.step < 4 ? '다음' : '완료'}
        </button>
      </div>
    );
  };

  const renderMonetizeModal = () => {
    if (!showMonetizeModal) return null;

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white p-6 rounded-lg">
          <h2 className="text-xl font-bold w-[300px] h-[50px] mb-4">지도핀 공개로 수익화를 하겠습니까?</h2>
          <div className="flex justify-end space-x-4">
            <button onClick={() => handleMonetizeChoice(true)} className="px-4 py-2 bg-green-500 text-white rounded">Yes</button>
            <button onClick={() => handleMonetizeChoice(false)} className="px-4 py-2 bg-red-500 text-white rounded">No</button>
          </div>
        </div>
      </div>
    );
  };

  const renderConfirmModal = () => {
    if (!showConfirmModal) return null;

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white p-6 rounded-lg">
          <h2 className="text-xl font-bold w-[300px] h-[50px] mb-4">수익화를 포기하겠습니까?</h2>
          <div className="flex justify-end space-x-4">
            <button onClick={() => handleConfirmChoice(true)} className="px-4 py-2 bg-green-500 text-white rounded">Yes</button>
            <button onClick={() => handleConfirmChoice(false)} className="px-4 py-2 bg-red-500 text-white rounded">No</button>
          </div>
        </div>
      </div>
    );
  };

  const renderAdMonetizeModal = () => {
    if (!showAdMonetizeModal) return null;
  
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white p-6 rounded-lg max-w-sm mx-4">
          <h2 className="text-xl font-bold mb-4">자신의 기존 광고 채널을 등록하세요</h2>
          <div className="flex flex-col space-y-2">
              <label className="block text-sm text-gray-600">기존 판매 채널 링크</label>
              <input
                type="text"
                className="w-full border p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                placeholder="Channel URL-1"
                value={state.platformLinks['youtube'] || ''}
                onChange={(e) => handlePlatformLinkChange(e, 'youtube')}
              />
              <input
                type="text"
                className="w-full border p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                placeholder="Channel URL-2"
                value={state.platformLinks['instagram'] || ''}
                onChange={(e) => handlePlatformLinkChange(e, 'instagram')}
              />
              <p className="p-2 text-sm text-gray-500">최대 2개 작성</p>
          </div>
          <div className="flex justify-end space-x-4">
            <button
              onClick={() => handleMonetizeChoice(true)}
              className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 transition-colors duration-300"
            >
              예
            </button>
            <button
              onClick={() => handleMonetizeChoice(false)}
              className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition-colors duration-300"
            >
              아니오
            </button>
          </div>
        </div>
      </div>
    );
  };


  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex flex-col items-center justify-center z-50 modal-overlay" onClick={handleOutsideClick}>
      <div className="absolute inset-0 bg-black opacity-50 modal-overlay"></div>
        <div className="relative bg-slate-50	 rounded-lg shadow-lg w-full max-w-lg p-2 mx-4 md:mx-0 md:max-w-xl z-10 flex flex-col"
        style={{ height: '90vh' , ...wallpaperStyles[wallpaper]}}>
        
        <div
            className="relative bg-white rounded-lg shadow-lg max-w-lg p-2 md:mx-2  md:max-w-xl z-10 flex flex-col"
            style={{
              height: `87vh`, // padding을 제외한 높이
              backgroundColor: 'white', // 배경색
            }}
          >
            <div className="flex justify-between items-center pb-3 border-b border-gray-200">
              <div className="text-xl font-semibold text-purple-500">새 핀스토리 작성</div>
              <button onClick={closeModal} className="text-gray-500 hover:text-gray-800">
                &times;
              </button>
            </div>
          <div className='mt-4 flex-1 overflow-auto'>
            {renderStepIndicator()}
            {renderStep()}
          </div>
          <div className="mt-4"> 
            {renderStepButtons()}
          </div>
          </div>
        </div>
        {renderAdMonetizeModal()}
      {renderMonetizeModal()}
      {renderConfirmModal()}
    </div>
  );
}
