"use client";
import React, { useState, useRef } from 'react';
import InstagramIcon from '/public/instagram_icon.svg';
import KakaoTalkIcon from '/public/kakaotalk_icon.svg';
import LineIcon from '/public/line_icon.svg';
import ThreadsIcon from '/public/threads_icon.svg';
import { FaCamera, FaPen, FaTags, FaShare, FaCheck, FaImage, FaVideo } from 'react-icons/fa';
import { MdKeyboardArrowRight } from "react-icons/md";
import usePostCreation from '@/app/components/logic/writeSeq';

const defaultTags = ['팝니다', '삽니다', '고칩니다', '알립니다'];

const steps = [
  { icon: <FaCamera />, label: '미디어' },
  { icon: <FaPen />, label: '내용' },
  { icon: <FaShare />, label: '설정' },
  { icon: <FaTags />, label: '태그' },
  { icon: <FaCheck />, label: '공유' },
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
  } = usePostCreation();

  const [selectedDefaultTags, setSelectedDefaultTags] = useState([]);
  const [bundleType, setBundleType] = useState('');
  const [newTags, setNewTags] = useState([]);
  const [showMonetizeModal, setShowMonetizeModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [industryType, setIndustryType] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');

  const fileInputRef = useRef(null);

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
    if (state.type !== '광고' && !showMonetizeModal) {
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
    setSelectedDefaultTags(prev => 
      prev.includes(tag) ? prev.filter(t => t !== tag) : [...prev, tag]
    );
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
      setMonetize(true);
      const fileUrls = state.imageFiles.map(file => URL.createObjectURL(file));
      addPin(fileUrls, true);
      closeModal();
    } else {
      setShowConfirmModal(true);
    }
  };

  const handleConfirmChoice = (choice) => {
    if (choice) {
      setMonetize(false);
      const fileUrls = state.imageFiles.map(file => URL.createObjectURL(file));
      addPin(fileUrls, false);
      closeModal();
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


  const renderStepIndicator = () => (
    <div className="flex mb-6 justify-start overflow-y-auto">
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
          <div className="space-y-4">
            <select
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
              value={state.type}
              onChange={handleTypeChange}
            >
              <option value="광고">광고</option>
              <option value="여행메모">여행메모</option>
              <option value="리뷰">리뷰</option>
            </select>
            {state.type === '여행메모' && (
              <textarea
                className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                rows="10"
                placeholder="여행 기록을 입력하세요..."
                value={state.content}
                onChange={handleContentChange}
              ></textarea>
            )}
            {state.type === '리뷰' && (
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
            {state.type === '광고' && (
            <div className="space-y-4">
              <select
                className="w-full border rounded-lg p-2"
                value={selectedCategory}
                onChange={handleCategoryChange}
              >
                <option value="">선택</option>
                <option value="유통">유통</option>
                <option value="요식업">요식업</option>
                <option value="이동형 판매">이동형 판매</option>
                <option value="서비스업">서비스업</option>
              </select>
              {selectedCategory === '유통' && (
                <div className="space-y-4">
                  <input
                    type="text"
                    placeholder="상품명 입력"
                    onChange={(e) => setIndustryType(e.target.value)}
                    className="w-full h-12 border rounded-lg p-2"
                  />
                  <input
                    type="number"
                    placeholder="재고 수량 입력"
                    onChange={(e) => setStock(e.target.value)}
                    className="w-full h-12 border rounded-lg p-2"
                  />
                  <div className="flex flex-col space-y-4">
                    <label className="text-sm">할인 형태 선택</label>
                    <select onChange={(e) => setDiscountType(e.target.value)} className="w-full border rounded-lg p-2">
                      <option value="">선택</option>
                      <option value="단순 할인">단순 할인</option>
                      <option value="묶음 할인">묶음 할인</option>
                    </select>
                  </div>
                  {state.discountType === '단순 할인' && (
                    <input
                      type="text"
                      placeholder="할인율 또는 할인가 입력"
                      onChange={(e) => setDiscountValue(e.target.value)}
                      className="w-full h-12 border rounded-lg p-2"
                    />
                  )}
                  {state.discountType === '묶음 할인' && (
                    <div>
                      <label className="text-sm">묶음 할인 유형 선택</label>
                      <select onChange={(e) => setBundleType(e.target.value)} className="w-full border rounded-lg p-2">
                        <option value="">선택</option>
                        <option value="1+1">1+1</option>
                        <option value="2+1">2+1</option>
                        <option value="N+1">N+1</option>
                      </select>
                      {bundleType === '1+1' && (
                        <input
                          type="text"
                          placeholder="1+1 설정"
                          onChange={(e) => setBundleDiscountValue(e.target.value)}
                          className="w-full h-12 border rounded-lg p-2"
                        />
                      )}
                      {bundleType === '2+1' && (
                        <input
                          type="text"
                          placeholder="2+1 설정"
                          onChange={(e) => setBundleDiscountValue(e.target.value)}
                          className="w-full h-12 border rounded-lg p-2"
                        />
                      )}
                      {bundleType === 'N+1' && (
                        <input
                          type="text"
                          placeholder="N값 입력 및 설정"
                          onChange={(e) => setBundleDiscountValue(e.target.value)}
                          className="w-full h-12 border rounded-lg p-2"
                        />
                      )}
                    </div>
                  )}
                  <input
                    type="text"
                    placeholder="할인 기간 설정"
                    className="w-full h-12 border rounded-lg p-2"
                  />
              </div>
              )}
            </div>
          )}
          </div>
        );
      case 3:
        return (
          state.type === '광고' && (
            <div className="px-4 md:px-6 space-y-4">
              <div className="flex flex-col space-y-4">
                <label className="text-sm">광고 옵션 설정</label>
                <select onChange={(e) => setAdOption(e.target.value)} className="w-full border rounded-lg p-2">
                  <option value="">선택</option>
                  <option value="광고1">광고1</option>
                  <option value="광고2">광고2</option>
                </select>
                <input
                  type="text"
                  placeholder="알림 반경 선택"
                  onChange={(e) => setRadius(e.target.value)}
                  className="w-full h-12 border rounded-lg p-2"
                />
                <label className="text-sm">광고 부스트 사용 여부</label>
                <select onChange={(e) => setBoost(e.target.value)} className="w-full border rounded-lg p-2">
                  <option value="">선택</option>
                  <option value="사용">사용</option>
                  <option value="사용 안함">사용 안함</option>
                </select>
                <input
                  type="text"
                  placeholder="지도에서 위치 선택"
                  onChange={(e) => setLocation(e.target.value)}
                  className="w-full h-12 border rounded-lg p-2"
                />
                <input
                  type="text"
                  placeholder="결제 정보 입력"
                  onChange={(e) => setPayment(e.target.value)}
                  className="w-full h-12 border rounded-lg p-2"
                />
              </div>
            </div>
          )
        );
      case 4:
        return (
          <div className="space-y-4">
          <div className="space-y-2">
            <label className="block mb-2 text-sm text-gray-600">태그 (최대 10개)</label>
            <div className="flex flex-wrap">
              {defaultTags.map(tag => (
                <button
                  key={tag}
                  className={`p-2 m-1 rounded-md ${selectedDefaultTags.includes(tag) ? 'bg-purple-500 text-white' : 'bg-gray-200 text-gray-700'} focus:outline-none`}
                  onClick={() => handleDefaultTagClick(tag)}
                >
                  {tag}
                </button>
              ))}
            </div>
            <div className="flex flex-wrap ">
              {newTags.map(tag => (
                <div key={tag} className="flex items-center bg-purple-500 text-white p-2 m-1 rounded-md">
                  <span>{tag}</span>
                  <button onClick={() => removeNewTag(tag)} className="ml-2 text-xs">&times;</button>
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
        );
      case 5:
        return (
          <div className="space-y-4">
            <label className="block mb-2 text-sm text-gray-600">SNS 공유</label>
            <div className="flex space-x-2">
              <button className="p-2 rounded bg-yellow-300 text-white"><KakaoTalkIcon className="w-6 h-6" /></button>
              <button className="p-2 rounded bg-green-300 text-white"><LineIcon className="w-6 h-6" /></button>
              <button className="p-2 rounded bg-pink-300 text-white"><InstagramIcon className="w-6 h-6" /></button>
              <button className="p-2 rounded bg-gray-300 text-white"><ThreadsIcon className="w-6 h-6" /></button>
            </div>
            <div className="flex flex-col space-y-2">
              <label className="block text-sm text-gray-600">YouTube 프로필</label>
              <input
                type="text"
                className="w-full border p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                placeholder="YouTube URL"
                value={state.platformLinks['youtube'] || ''}
                onChange={(e) => handlePlatformLinkChange(e, 'youtube')}
              />
              <label className="block text-sm text-gray-600">Instagram 프로필</label>
              <input
                type="text"
                className="w-full border p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                placeholder="Instagram URL"
                value={state.platformLinks['instagram'] || ''}
                onChange={(e) => handlePlatformLinkChange(e, 'instagram')}
              />
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  const renderStepButtons = () => {
    return (
      <div className="mt-6 flex justify-between">
        <button
          onClick={() => setStep(Math.max(1, state.step - 1))}
          className={`px-4 py-2 text-gray-600 rounded ${state.step === 1 ? 'bg-gray-100 cursor-not-allowed' : 'bg-gray-200 hover:bg-gray-300'}`}
          disabled={state.step === 1}
        >
          이전
        </button>
        <button
          onClick={() => state.step < 5 ? setStep(state.step + 1) : handlePost()}
          className="px-4 py-2 bg-purple-500 text-white rounded hover:bg-purple-600 transition-colors duration-300"
        >
          {state.step < 5 ? '다음' : '완료'}
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


  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex flex-col items-center justify-center z-50 modal-overlay" onClick={handleOutsideClick}>
      <div className="absolute inset-0 bg-black opacity-50 modal-overlay"></div>
        <div className="relative bg-white rounded-lg shadow-lg w-full max-w-lg p-4 mx-4 md:mx-0 md:max-w-xl z-10 flex flex-col" style={{ height: '90vh' }}>
          <div className="flex justify-between items-center pb-3 border-b border-gray-200">
            <h3 className="text-xl font-semibold text-purple-500">새 핀스토리 작성</h3>
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
      {renderMonetizeModal()}
      {renderConfirmModal()}
    </div>
  );
}
