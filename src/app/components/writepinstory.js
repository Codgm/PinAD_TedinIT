"use client";
import React, { useState, useRef, useEffect } from 'react';
import { FaCamera, FaPen, FaTags, FaShare, FaCheck, FaImage, FaVideo } from 'react-icons/fa';
import { MdKeyboardArrowRight } from "react-icons/md";
import usePostCreation from '@/app/components/logic/writeSeq';
import AdStepComponent from './AdStepComponent';
import Styles from '@/app/styles/writepinstory.module.css';
import EventNotification from './eventnotifcomponent';

const steps = [
  { icon: <FaCamera />, label: '미디어' },
  { icon: <FaPen />, label: '내용' },
  { icon: <FaTags />, label: '태그' },
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

  const fileInputRef = useRef(null);
  const dropdownRef = useRef(null);

  useEffect(() => {
    if (dropdownRef.current) {
      const options = dropdownRef.current.querySelectorAll('option');
      // 스크롤 애니메이션 적용
      dropdownRef.current.classList.add('spinner');

      // 애니메이션이 끝나면 class 제거
      const animationDuration = 1000; // 애니메이션 시간 (1초)
      const timer = setTimeout(() => {
        dropdownRef.current.classList.remove('spinner');
      }, animationDuration);

      // 컴포넌트 언마운트 시 타이머 정리
      return () => clearTimeout(timer);
    }
  }, [state.selectedCategory]);

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
      addPin(fileUrls, state.type === '광고' ? true : state.monetize);
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
      const confirmation = window.confirm('광고를 실행하고 핀을 꽂을까요?');
      if (confirmation) {
        setMonetize(false);
        const fileUrls = state.imageFiles.map(file => URL.createObjectURL(file));
        addPin(fileUrls, false);
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
      const confirmation = window.confirm('광고를 실행하고 핀을 꽂을까요?');
      if (confirmation) {
        setMonetize(false);
        const fileUrls = state.imageFiles.map(file => URL.createObjectURL(file));
        addPin(fileUrls, false);
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
      backgroundColor: '#d0936d', /* 붉은색 배경 */
      backgroundImage: `
        linear-gradient(0deg, rgba(255, 255, 255, 0.2) 1px, transparent 1px), /* 수평 스트라이프 */
        linear-gradient(90deg, rgba(255, 255, 255, 0.2) 1px, transparent 1px), /* 수직 스트라이프 */
      `,
      backgroundSize: '20px 20px, 20px 20px, 60px 60px', /* 각 패턴의 크기 */
      backgroundPosition: '0 0, 0 0, 0 0', /* 각 패턴의 위치 */
      backgroundRepeat: 'repeat', /* 배경 반복 설정 */      // backgroundImage: 'url(/box.jpg)',
      // backgroundSize: 'contain',
      // backgroundRepeat: 'norepeat',
    },
    "행사알림": {
      backgroundColor: '#d0936d', /* 붉은색 배경 */
      backgroundImage: `
        linear-gradient(0deg, rgba(255, 255, 255, 0.2) 1px, transparent 1px), /* 수평 스트라이프 */
        linear-gradient(90deg, rgba(255, 255, 255, 0.2) 1px, transparent 1px), /* 수직 스트라이프 */
      `,
      backgroundSize: '20px 20px, 20px 20px, 60px 60px', /* 각 패턴의 크기 */
      backgroundPosition: '0 0, 0 0, 0 0', /* 각 패턴의 위치 */
      backgroundRepeat: 'repeat', /* 배경 반복 설정 */      // backgroundImage: 'url(/box.jpg)',
      // backgroundSize: 'contain',
      // backgroundRepeat: 'norepeat',
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
      backgroundColor: '#8B4513', /* 벽돌 색상 */
      backgroundImage: `
        linear-gradient(90deg, rgba(255,255,255,.07) 50%, transparent 50%),
        linear-gradient(90deg, rgba(255,255,255,.13) 50%, transparent 50%),
        linear-gradient(90deg, transparent 50%, rgba(255,255,255,.17) 50%),
        linear-gradient(90deg, transparent 50%, rgba(255,255,255,.19) 50%)
      `,
      backgroundSize: '13px 13px, 29px 29px, 37px 37px, 53px 53px', /* 각 gradient의 크기 설정 */
      backgroundPosition: '0 0, 0 0, 0 0, 0 0', /* 각 gradient의 위치 설정 */
      backgroundRepeat: 'repeat', /* 배경 반복 설정 */
    },
    "구인": {
      backgroundColor: '#4CAF50',
      backgroundImage: 'linear-gradient(135deg, #45a247 25%, transparent 25%), linear-gradient(225deg, #45a247 25%, transparent 25%), linear-gradient(45deg, #45a247 25%, transparent 25%), linear-gradient(315deg, #45a247 25%, #4CAF50 25%)',
      backgroundPosition: '10px 0, 10px 0, 0 0, 0 0',
      backgroundSize: '20px 20px',
      backgroundRepeat: 'repeat'
    }
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
        wallpaper =="" ? <span className="text-l  p-2 text-white font-bold"/> :<span className="text-l  p-2 rounded-full text-white font-bold shadow-md" style={{...wallpaperStyles[wallpaper]}}>{wallpaper}</span>
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
            <div className="flex items-center w-full space-x-2">
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
            </div>
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
                <option value="약속 장소">약속장소</option>
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
              <textarea
                className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                rows="10"
                placeholder="여행 기록을 입력하세요..."
                value={state.content}
                onChange={handleContentChange}
              ></textarea>
            )}
            {state.selectedCategory === '리뷰' && state.type === '핀스토리' && (
              <div className="space-y-4">
                <textarea
                  className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                  rows="5"
                  placeholder="리뷰 내용을 입력하세요..."
                  value={state.content}
                  onChange={handleContentChange}
                ></textarea>
                <input
                  type="number"
                  min="1"
                  max="5"
                  className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                  placeholder="평점 (1-5)"
                  value={state.rating}
                  onChange={(e) => setRating(e.target.value)}
                />
              </div>
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
            setLocation={setLocation}
            setStartDate={setStartDate}
            setEndDate={setEndDate}
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
        <div className="relative bg-white rounded-lg shadow-lg w-full max-w-lg p-2 mx-4 md:mx-0 md:max-w-xl z-10 flex flex-col" style={{ height: '90vh' , ...wallpaperStyles[wallpaper]}}>
        
        <div
            className="relative bg-white rounded-lg shadow-lg max-w-lg p-2 md:mx-2  md:max-w-xl z-10 flex flex-col"
            style={{
              height: `87vh`, // padding을 제외한 높이
              backgroundColor: '#f0f0f0', // 배경색
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
