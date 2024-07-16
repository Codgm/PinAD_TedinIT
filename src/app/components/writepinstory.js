"use client";
import React, { useState } from 'react';
import InstagramIcon from '/public/instagram_icon.svg';
import YouTubeIcon from '/public/youtube_icon.svg';
import TikTokIcon from '/public/tiktok_logo_brand_icon.svg';

export default function WritePinStory({ isOpen, closeModal, addPin }) {
  const [content, setContent] = useState('');
  const [image, setImage] = useState(null);
  const [shortVideo, setShortVideo] = useState(null);
  const [platforms, setPlatforms] = useState([]);
  const [profilePublic, setProfilePublic] = useState(false);
  const [platformLinks, setPlatformLinks] = useState({});
  const [showPlatformSelection, setShowPlatformSelection] = useState(false);

  const handleContentChange = (e) => setContent(e.target.value);
  const handleImageChange = (e) => setImage(e.target.files[0]);
  const handleShortVideoChange = (e) => setShortVideo(e.target.files[0]);
  
  const togglePlatform = (platform) => {
    if (platforms.includes(platform)) {
      setPlatforms(platforms.filter((p) => p !== platform));
    } else {
      setPlatforms([...platforms, platform]);
    }
  };

  const toggleProfileVisibility = () => setProfilePublic(!profilePublic);

  const handlePost = () => {
    const pinData = {
      content,
      image,
      shortVideo,
      platforms: [...platforms],
      platformLinks: { ...platformLinks },
      profilePublic,
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

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="absolute inset-0 bg-black opacity-50" onClick={closeModal}></div>
      <div className="bg-white rounded-lg shadow-lg w-full max-w-lg p-6 mx-4 md:mx-0 md:max-w-2xl z-10 overflow-auto max-h-[90vh]">
        <div className="flex justify-between items-center pb-3 border-b">
          <h3 className="text-xl font-semibold text-purple-500">핀 스토리 작성</h3>
          <button onClick={closeModal} className="text-gray-500 hover:text-gray-800">
            &times;
          </button>
        </div>
        <div className="mt-4 space-y-4">
          <textarea
            className="w-full p-2 border rounded-md"
            rows="5"
            placeholder="문구 입력..."
            value={content}
            onChange={handleContentChange}
          ></textarea>
          <div className="space-y-2">
            <label className="block mb-2 text-sm text-gray-600">이미지 업로드</label>
            <input
              type="file"
              className="w-full border p-2 rounded-md"
              onChange={handleImageChange}
            />
          </div>
          <div className="space-y-2">
            <label className="block mb-2 text-sm text-gray-600">숏폼 비디오 업로드</label>
            <input
              type="file"
              className="w-full border p-2 rounded-md"
              onChange={handleShortVideoChange}
            />
          </div>
          <div className="space-y-2">
            <button
              className="block mb-2 text-sm text-gray-900 flex items-center"
              onClick={togglePlatformSelection}
            >
              플랫폼
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" className="w-4 h-4 ml-1">
                    <path stroke-linecap="round" stroke-linejoin="round" d="m19.5 8.25-7.5 7.5-7.5-7.5" />
                </svg>
            </button>
            {showPlatformSelection && (
              <div className="space-y-2">
                <button
                  className={`flex items-center ${platforms.includes('instagram') ? 'bg-gray-200' : 'bg-white'}`}
                  onClick={() => togglePlatform('instagram')}
                >
                  <InstagramIcon className="w-6 h-6 mr-2" />
                  <span className="text-sm">Instagram</span>
                </button>
                {platforms.includes('instagram') && (
                  <input
                    type="text"
                    className="w-full border p-2 rounded-md"
                    placeholder="Instagram URL"
                    value={platformLinks['instagram'] || ''}
                    onChange={(e) => handlePlatformLinkChange(e, 'instagram')}
                  />
                )}

                <button
                  className={`flex items-center ${platforms.includes('youtube') ? 'bg-gray-200' : 'bg-white'}`}
                  onClick={() => togglePlatform('youtube')}
                >
                  <YouTubeIcon className="w-6 h-6 mr-2" />
                  <span className="text-sm">YouTube</span>
                </button>
                {platforms.includes('youtube') && (
                  <input
                    type="text"
                    className="w-full border p-2 rounded-md"
                    placeholder="YouTube URL"
                    value={platformLinks['youtube'] || ''}
                    onChange={(e) => handlePlatformLinkChange(e, 'youtube')}
                  />
                )}

                <button
                  className={`flex items-center ${platforms.includes('tiktok') ? 'bg-gray-200' : 'bg-white'}`}
                  onClick={() => togglePlatform('tiktok')}
                >
                  <TikTokIcon className="w-6 h-6 mr-2" />
                  <span className="text-sm">TikTok</span>
                </button>
                {platforms.includes('tiktok') && (
                  <input
                    type="text"
                    className="w-full border p-2 rounded-md"
                    placeholder="TikTok URL"
                    value={platformLinks['tiktok'] || ''}
                    onChange={(e) => handlePlatformLinkChange(e, 'tiktok')}
                  />
                )}
              </div>
            )}
          </div>
          <div className="space-y-2">
            <label className="flex items-center">
              <input
                type="checkbox"
                className="mr-2"
                checked={profilePublic}
                onChange={toggleProfileVisibility}
              />
              <span className="text-sm text-gray-600">프로필 공개</span>
            </label>
          </div>
        </div>
        <div className="mt-4 flex justify-end space-x-2">
          <button
            onClick={closeModal}
            className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400 transition-colors duration-300"
          >
            취소
          </button>
          <button
            onClick={handlePost}
            className="px-4 py-2 bg-purple-500 text-white rounded hover:bg-purple-600 transition-colors duration-300"
          >
            게시
          </button>
        </div>
      </div>
    </div>
  );
}
