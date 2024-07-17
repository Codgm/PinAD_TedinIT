"use client";
import React, { useState } from 'react';
import InstagramIcon from '/public/instagram_icon.svg';
import YouTubeIcon from '/public/youtube_icon.svg';
import TikTokIcon from '/public/tiktok_logo_brand_icon.svg';
import KakaoTalkIcon from '/public/kakaotalk_icon.svg';
import LineIcon from '/public/line_icon.svg';
import ThreadsIcon from '/public/threads_icon.svg';

const recommendedTags = ['여행', '맛집', '자연', '힐링', '액티비티', '문화', '역사', '쇼핑', '숙소', '교통'];

export default function WritePinStory({ isOpen, closeModal, addPin }) {
  const [step, setStep] = useState(1);
  const [title, setTitle] = useState('');
  const [type, setType] = useState('광고');
  const [content, setContent] = useState('');
  const [imageFiles, setImageFiles] = useState([]);
  const [shortVideo, setShortVideo] = useState(null);
  const [platforms, setPlatforms] = useState([]);
  const [profilePublic, setProfilePublic] = useState(false);
  const [platformLinks, setPlatformLinks] = useState({});
  const [showPlatformSelection, setShowPlatformSelection] = useState(false);
  const [tags, setTags] = useState([]);
  const [monetize, setMonetize] = useState(null);

  const handleTitleChange = (e) => setTitle(e.target.value);
  const handleTypeChange = (e) => setType(e.target.value);
  const handleContentChange = (e) => setContent(e.target.value);
  const handleImageChange = (e) => {
    if (e.target.files.length + imageFiles.length <= 4) {
      setImageFiles([...imageFiles, ...Array.from(e.target.files)]);
    }
  };
  const handleShortVideoChange = (e) => {
    const file = e.target.files[0];
    if (file && file.size <= 50 * 1024 * 1024) { // 50MB size limit
      setShortVideo(file);
    } else {
      alert('비디오 파일 크기는 50MB 이하로 업로드 해주세요.');
    }
  };

  const togglePlatform = (platform) => {
    if (platforms.includes(platform)) {
      setPlatforms(platforms.filter((p) => p !== platform));
    } else {
      setPlatforms([...platforms, platform]);
    }
  };

  const toggleProfileVisibility = () => setProfilePublic(!profilePublic);

  const handleOutsideClick = (e) => {
    if (e.target.classList.contains('modal-overlay')) {
      closeModal();
    }
  };

  const handlePost = () => {
    const pinData = {
      title,
      type,
      content,
      imageFiles,
      shortVideo,
      platforms: [...platforms],
      platformLinks: { ...platformLinks },
      profilePublic,
      tags,
      monetize,
    };
    addPin(pinData);
    closeModal();
  };

  const handlePlatformLinkChange = (e, platform) => {
    const value = e.target.value;
    setPlatformLinks({ ...platformLinks, [platform]: value });
  };

  const togglePlatformSelection = () => {
    setShowPlatformSelection(!showPlatformSelection);
  };

  const handleTagClick = (tag) => {
    if (tags.includes(tag)) {
      setTags(tags.filter(t => t !== tag));
    } else if (tags.length < 10) {
      setTags([...tags, tag]);
    }
  };

  const handleMonetizeChange = (value) => {
    setMonetize(value);
  };

  const renderStep = () => {
    switch (step) {
      case 1:
        return (
          <div className="space-y-4">
            <div className="space-y-2">
              <label className="block mb-2 text-sm text-gray-600">이미지 업로드 (최대 4장)</label>
              <input
                type="file"
                multiple
                accept="image/*"
                className="w-full border p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                onChange={handleImageChange}
              />
            </div>
            <div className="space-y-2">
              <label className="block mb-2 text-sm text-gray-600">숏폼 비디오 업로드 (최대 50MB)</label>
              <input
                type="file"
                accept="video/*"
                className="w-full border p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                onChange={handleShortVideoChange}
              />
            </div>
            <button onClick={() => setStep(2)} className="px-4 py-2 bg-purple-500 text-white rounded hover:bg-purple-600 transition-colors duration-300">
              다음
            </button>
          </div>
        );
      case 2:
        return (
          <div className="space-y-4">
            <input
              type="text"
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
              placeholder="제목"
              value={title}
              onChange={handleTitleChange}
            />
            <select
              className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
              value={type}
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
              value={content}
              onChange={handleContentChange}
            ></textarea>
            <button onClick={() => setStep(3)} className="px-4 py-2 bg-purple-500 text-white rounded hover:bg-purple-600 transition-colors duration-300">
              다음
            </button>
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
                    className={`p-2 m-1 rounded-md ${tags.includes(tag) ? 'bg-purple-500 text-white' : 'bg-gray-200 text-gray-700'} focus:outline-none`}
                    onClick={() => handleTagClick(tag)}
                  >
                    {tag}
                  </button>
                ))}
              </div>
            </div>
            <button onClick={() => setStep(4)} className="px-4 py-2 bg-purple-500 text-white rounded hover:bg-purple-600 transition-colors duration-300">
              다음
            </button>
          </div>
        );
      case 4:
        return (
          <div className="space-y-4">
            <button
              className="block mb-2 text-sm text-gray-900 flex items-center"
              onClick={togglePlatformSelection}
            >
            </button>
            <div className="flex items-center">
              <label className="text-sm text-gray-600 mr-2">지도핀을 공개하고 수익화하시겠습니까?</label>
              <button onClick={() => handleMonetizeChange(true)} className={`px-2 py-1 rounded ${monetize === true ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-700'}`}>예</button>
              <button onClick={() => handleMonetizeChange(false)} className={`px-2 py-1 ml-2 rounded ${monetize === false ? 'bg-red-500 text-white' : 'bg-gray-200 text-gray-700'}`}>아니오</button>
            </div>
            {monetize === false && (
              <p className="text-sm text-gray-600">언제든 공개하고 수익화 가능합니다. 현재 나만의 여정으로 등록됨.</p>
            )}
            <div className="space-y-2">
              <div className="flex items-center">
                <input
                  type="checkbox"
                  id="profilePublicCheckbox"
                  className="mr-2"
                  checked={profilePublic}
                  onChange={toggleProfileVisibility}
                />
                <label htmlFor="profilePublicCheckbox" className="text-sm text-gray-600 cursor-pointer">
                  프로필 공개
                </label>
              </div>
            </div>
            <button onClick={() => setStep(5)} className="px-4 py-2 bg-purple-500 text-white rounded hover:bg-purple-600 transition-colors duration-300">
              다음
            </button>
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
                value={platformLinks['youtube'] || ''}
                onChange={(e) => handlePlatformLinkChange(e, 'youtube')}
              />
              <label className="block text-sm text-gray-600">Instagram 프로필</label>
              <input
                type="text"
                className="w-full border p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                placeholder="Instagram URL"
                value={platformLinks['instagram'] || ''}
                onChange={(e) => handlePlatformLinkChange(e, 'instagram')}
              />
            </div>
            <div className="mt-4 flex justify-end space-x-2">
              <button onClick={handlePost} className="px-4 py-2 bg-purple-500 text-white rounded hover:bg-purple-600 transition-colors duration-300">
                글 작성 완료
              </button>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 modal-overlay" onClick={handleOutsideClick}>
      <div className="absolute inset-0 bg-black opacity-50 modal-overlay"></div>
      <div className="bg-white rounded-lg shadow-lg w-full max-w-lg p-6 mx-4 md:mx-0 md:max-w-2xl z-10 overflow-auto max-h-[90vh]">
        <div className="flex justify-between items-center pb-3 border-b border-gray-200">
          <h3 className="text-xl font-semibold text-purple-500">핀 스토리 작성</h3>
          <button onClick={closeModal} className="text-gray-500 hover:text-gray-800">
            &times;
          </button>
        </div>
        <div className="mt-4 space-y-4">
          {renderStep()}
        </div>
      </div>
    </div>
  );
}
