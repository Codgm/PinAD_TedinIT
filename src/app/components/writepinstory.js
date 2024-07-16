'use client';
import { useState } from 'react';

export default function WritePinStory({ isOpen, closeModal, addPin }) {
  const [content, setContent] = useState('');
  const [image, setImage] = useState(null);
  const [shortVideo, setShortVideo] = useState(null);
  const [instagram, setInstagram] = useState('');
  const [youtube, setYoutube] = useState('');
  const [tiktok, setTiktok] = useState('');
  const [profile, setProfile] = useState('');

  const handleContentChange = (e) => setContent(e.target.value);
  const handleImageChange = (e) => setImage(e.target.files[0]);
  const handleShortVideoChange = (e) => setShortVideo(e.target.files[0]);
  const handleInstagramChange = (e) => setInstagram(e.target.value);
  const handleYoutubeChange = (e) => setYoutube(e.target.value);
  const handleTiktokChange = (e) => setTiktok(e.target.value);
  const handleProfileChange = (e) => setProfile(e.target.value);

  const handlePost = () => {
    addPin({ content, image, shortVideo, instagram, youtube, tiktok, profile });
    closeModal();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="absolute inset-0 bg-black opacity-50" onClick={closeModal}></div>
      <div className="bg-white rounded-lg shadow-lg w-full max-w-lg p-6 mx-4 md:mx-0 md:max-w-2xl z-10 overflow-auto max-h-[90vh]">
        <div className="flex justify-between items-center pb-3 border-b">
          <h3 className="text-xl font-semibold text-purple-500">핀 스토리 작성</h3>
          <button onClick={closeModal} className="text-gray-500 hover:text-gray-800">&times;</button>
        </div>
        <div className="mt-4 space-y-4">
          <textarea className="w-full p-2 border rounded-md" rows="5" placeholder="문구 입력..." value={content} onChange={handleContentChange}></textarea>
          <div>
            <label className="block mb-2 text-sm text-gray-600">이미지 업로드</label>
            <input type="file" className="w-full border p-2 rounded-md" onChange={handleImageChange} />
          </div>
          <div>
            <label className="block mb-2 text-sm text-gray-600">숏폼 비디오 업로드</label>
            <input type="file" className="w-full border p-2 rounded-md" onChange={handleShortVideoChange} />
          </div>
          <div>
            <label className="block mb-2 text-sm text-gray-600">인스타그램</label>
            <input type="text" className="w-full border p-2 rounded-md" placeholder="Instagram URL" value={instagram} onChange={handleInstagramChange} />
          </div>
          <div>
            <label className="block mb-2 text-sm text-gray-600">유튜브</label>
            <input type="text" className="w-full border p-2 rounded-md" placeholder="YouTube URL" value={youtube} onChange={handleYoutubeChange} />
          </div>
          <div>
            <label className="block mb-2 text-sm text-gray-600">틱톡</label>
            <input type="text" className="w-full border p-2 rounded-md" placeholder="TikTok URL" value={tiktok} onChange={handleTiktokChange} />
          </div>
          <div>
            <label className="block mb-2 text-sm text-gray-600">프로필</label>
            <input type="text" className="w-full border p-2 rounded-md" placeholder="Profile URL" value={profile} onChange={handleProfileChange} />
          </div>
        </div>
        <div className="mt-4 flex justify-end space-x-2">
          <button onClick={closeModal} className="px-4 py-2 bg-gray-300 rounded">취소</button>
          <button onClick={handlePost} className="px-4 py-2 bg-purple-500 text-white rounded">게시</button>
        </div>
      </div>
    </div>
  );
}
