"use client";
import React, { useState, useRef } from 'react';
import InstagramIcon from '/public/instagram_icon.svg';
import KakaoTalkIcon from '/public/kakaotalk_icon.svg';
import LineIcon from '/public/line_icon.svg';
import ThreadsIcon from '/public/threads_icon.svg';
import { FaCamera, FaPen, FaTags, FaShare, FaCheck, FaImage, FaVideo } from 'react-icons/fa';
import { MdKeyboardArrowRight } from "react-icons/md";
import usePostCreation from '@/app/components/logic/writeSeq';

const recommendedTags = ['여행', '맛집', '자연', '힐링', '액티비티', '문화', '역사', '쇼핑', '숙소', '교통'];

const steps = [
  { icon: <FaCamera />, label: '미디어' },
  { icon: <FaPen />, label: '내용' },
  { icon: <FaTags />, label: '태그' },
  { icon: <FaShare />, label: '설정' },
  { icon: <FaCheck />, label: '공유' },
];

export default function WritePinStory({ isOpen, closeModal, addPin }) {
  const {
    state,
    setStep,
    setTitle,
    setType,
    setContent,
    addImage,
    setShortVideo,
    clearErrors,
    setPlatforms,
    setProfilePublic,
    setPlatformLink,
    togglePlatformSelection,
    addTag,
    setMonetize
  } = usePostCreation();

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
    console.log(state.imageFiles)
    const fileUrls = state.imageFiles.map(file => URL.createObjectURL(file));
    addPin(fileUrls,true);
    closeModal();
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

  const handleTagClick = (tag) => {
    addTag(tag);
  };

  const handleFileUpload = (e) => {
    const files = Array.from(e.target.files);
    files.forEach(file => {
      if (file.type.startsWith('image/')) {
        if (state.imageFiles.length < 4) {
          addImage(file);
        }
      } else if (file.type.startsWith('video/')) {
        if (file.size <= 50 * 1024 * 1024) { // 50MB 제한
          setShortVideo(file);
        } else {
          alert('비디오 파일 크기는 50MB 이하로 업로드 해주세요.');
        }
      }
    });
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
                {<MdKeyboardArrowRight/>}
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
            <input
              type="text"
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
              placeholder="제목"
              value={state.title}
              onChange={handleTitleChange}
            />
            <select
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
              value={state.type}
              onChange={handleTypeChange}
            >
              <option value="광고">광고</option>
              <option value="여행메모">여행메모</option>
              <option value="리뷰">리뷰</option>
            </select>
            <textarea
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
              rows="5"
              maxLength="500"
              placeholder="문구 입력... (최대 500자)"
              value={state.content}
              onChange={handleContentChange}
            ></textarea>
          </div>
        );
      case 3:
        return (
          <div className="space-y-4">
            <div className="space-y-2">
              <label className="block mb-2 text-sm text-gray-600">태그 (최대 10개)</label>
              <div className="flex flex-wrap">
                {recommendedTags.map(tag => (
                  <button
                    key={tag}
                    className={`p-2 m-1 rounded-md ${state.tags.includes(tag) ? 'bg-purple-500 text-white' : 'bg-gray-200 text-gray-700'} focus:outline-none`}
                    onClick={() => handleTagClick(tag)}
                  >
                    {tag}
                  </button>
                ))}
              </div>
            </div>
          </div>
        );
      case 4:
        return (
          <div className="space-y-4">
            <div className="flex items-center">
              <label className="text-sm text-gray-600 mr-2">지도핀을 공개하고 수익화하시겠습니까?</label>
              <button onClick={() => setMonetize(true)} className={`px-2 py-1 rounded ${state.monetize === true ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-700'}`}>예</button>
              <button onClick={() => setMonetize(false)} className={`px-2 py-1 ml-2 rounded ${state.monetize === false ? 'bg-red-500 text-white' : 'bg-gray-200 text-gray-700'}`}>아니오</button>
            </div>
            {state.monetize === false && (
              <p className="text-sm text-gray-600">언제든 공개하고 수익화 가능합니다. 현재 나만의 여정으로 등록됨.</p>
            )}
            <div className="space-y-2">
              <div className="flex items-center">
                <input
                  type="checkbox"
                  id="profilePublicCheckbox"
                  className="mr-2"
                  checked={state.profilePublic}
                  onChange={() => setProfilePublic(!state.profilePublic)}
                />
                <label htmlFor="profilePublicCheckbox" className="text-sm text-gray-600 cursor-pointer">
                  프로필 공개
                </label>
              </div>
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
    </div>
  );
}
