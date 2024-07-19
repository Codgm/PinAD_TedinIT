
import React, { useState } from 'react';

const PinstoryModal = ({ pin, onClose, updatePin }) => {
  const [likeCount, setLikeCount] = useState(pin.likes || 0);
  const [comments, setComments] = useState(pin.comments || []);
  const [newComment, setNewComment] = useState('');

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
          {pin.image ? (
            <img
              src={URL.createObjectURL(pin.image)}
              alt="Pin Image"
              className="w-full h-auto object-cover rounded-md"
            />
          ) : (
            <div className="w-full h-72 bg-gray-200 flex items-center justify-center text-gray-600 rounded-md">
              400 x 300
            </div>
          )}
        </div>
        <p className="text-gray-700 mb-4">{pin.content || '핀스토리 내용이 없습니다.'}</p>
        <div className="flex gap-4 mb-4">
          <button
            className="flex items-center gap-2 text-gray-700 hover:text-gray-900"
            onClick={handleLike}
          >
            <span role="img" aria-label="heart">♡</span> {likeCount}
          </button>
          <button className="flex items-center gap-2 text-gray-700 hover:text-gray-900">
            <span role="img" aria-label="comment">💬</span> {comments.length}
          </button>
        </div>
        <div className="mb-4">
          <textarea
            className="w-full p-2 border border-gray-300 rounded-md resize-none"
            placeholder="댓글 작성..."
            value={newComment}
            onChange={handleCommentChange}
          ></textarea>
          <button
            className="mt-2 w-full py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600"
            onClick={handleAddComment}
          >
            댓글 달기
          </button>
        </div>
        <div className="space-y-2 mb-4">
          {comments.map((comment, index) => (
            <div key={index} className="p-2 border border-gray-300 rounded-md">
              {comment}
            </div>
          ))}
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

export default PinstoryModal;
