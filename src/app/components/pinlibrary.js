
import React, { useState } from 'react';

const PinLibraryModal = ({ pin, onClose, updatePin }) => {
  const [likeCount, setLikeCount] = useState(pin.likes || 0);
  const [comments, setComments] = useState(pin.comments || []);
  const [newComment, setNewComment] = useState('');

  const handleImageClick = (imageUrl) => {
    console.log('Clicked image URL:', imageUrl);
  };

  const handleLike = () => {
    const updatedLikes = likeCount + 1;
    setLikeCount(updatedLikes);
    updatePin({ ...pin, likes: updatedLikes });
  };

  const handleCommentChange = (e) => setNewComment(e.target.value);

  const handleAddComment = () => {
    if (newComment.trim()) {
      const updatedComments = [...comments, newComment];
      setComments(updatedComments);
      setNewComment('');
      updatePin({ ...pin, comments: updatedComments });
    }
  };

  const handleOutsideClick = (e) => {
    if (e.target.classList.contains('fixed') && e.target.classList.contains('inset-0')) {
      onClose();
    }
  };

  return (
    <div
      className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      onClick={handleOutsideClick}
    >
      <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold">사용자의 핀스토리</h2>
          <button
            className="text-xl font-bold text-gray-700 hover:text-gray-900"
            onClick={onClose}
          >
            ×
          </button>
        </div>
        <div className="mb-4">
            {!pin.images || pin.images.length === 0 ? (
                <div className="w-full h-72 bg-gray-200 flex items-center justify-center text-gray-600 rounded-md p-2">
                400 x 300
                </div>
            ) : pin.images.length === 1 ? (
                <div className="w-full">
                <img
                    src={pin.images[0]}
                    alt="Pin Image"
                    className="w-full h-72 object-cover rounded-md mb-2 cursor-pointer"
                    onClick={() => handleImageClick(pin.images[0])}
                />
                <div className="w-full h-36 bg-gray-200 flex items-center justify-center text-gray-600 rounded-md">
                    + 추가
                </div>
                </div>
            ) : pin.images.length === 2 ? (
                <div className="w-full">
                <img
                    src={pin.images[0]}
                    alt="Pin Image 1"
                    className="w-full h-72 object-cover rounded-md mb-2 cursor-pointer"
                    onClick={() => handleImageClick(pin.images[0])}
                />
                <div className="flex w-full">
                    <div className="w-1/2 pr-1">
                    <img
                        src={pin.images[1]}
                        alt="Pin Image 2"
                        className="w-full h-36 object-cover rounded-md cursor-pointer"
                        onClick={() => handleImageClick(pin.images[1])}
                    />
                    </div>
                    <div className="w-1/2 pl-1">
                    <div className="h-36 bg-gray-200 flex items-center justify-center text-gray-600 rounded-md">
                        + 추가
                    </div>
                    </div>
                </div>
                </div>
            ) : (
                <div className="w-full">
                <img
                    src={pin.images[0]}
                    alt="Pin Image 1"
                    className="w-full h-72 object-cover rounded-md mb-2 cursor-pointer"
                    onClick={() => handleImageClick(pin.images[0])}
                />
                <div className="flex w-full">
                    <div className="w-1/2 pr-1">
                    <img
                        src={pin.images[1]}
                        alt="Pin Image 2"
                        className="w-full h-36 object-cover rounded-md cursor-pointer"
                        onClick={() => handleImageClick(pin.images[1])}
                    />
                    </div>
                    <div className="w-1/2 pl-1">
                    <img
                        src={pin.images[2]}
                        alt="Pin Image 3"
                        className="w-full h-36 object-cover rounded-md cursor-pointer"
                        onClick={() => handleImageClick(pin.images[2])}
                    />
                    </div>
                </div>
                </div>
            )}
        </div>
        <div className="flex gap-4 mb-4">
        </div>
        <div className="flex gap-4">
          <button className="flex-1 py-2 bg-red-500 text-white rounded-md hover:bg-red-600">
            유튜브
          </button>
          <button className="flex-1 py-2 bg-pink-500 text-white rounded-md hover:bg-pink-600">
            인스타그램
          </button>
        </div>
      </div>
    </div>
  );
};

export default PinLibraryModal;
