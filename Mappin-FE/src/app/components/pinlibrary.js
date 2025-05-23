"use client"
import React, { useState, useEffect } from 'react';
import '@fortawesome/fontawesome-free/css/all.min.css';
import PinstoryModal from './pinstorymodal';
import Image from 'next/image';
import { useTranslation } from 'react-i18next';
import AdTemplate from './PinStoryTemplate/AdTemplate';
import EventNotificationTemplate from './PinStoryTemplate/EventNotificationTemplate';
import TravelRecord from './PinStoryTemplate/TravelRecordTemplate';
import Review from './PinStoryTemplate/ReviewTemplate';
import AttractionRecommendation from './PinStoryTemplate/AttractionRecommendationTemplate';
import AppointmentPlace from './PinStoryTemplate/AppointmentPlaceTemplate';
import FBTemplate from './PinStoryTemplate/F&BTemplate';



const templates = {
  '광고': {
    '유통': AdTemplate,
    'F&B': FBTemplate,
    '행사알림': EventNotificationTemplate,
  },
  '핀스토리': {
    '리뷰': Review,
    '명소추천': AttractionRecommendation,
    '약속장소': AppointmentPlace,
    '여행기록': TravelRecord,
  },
};


const PinLibraryModal = ({ pin, onClose, updatePin }) => {

  const { t } = useTranslation();

  const [likeCount, setLikeCount] = useState(pin.likes || 0);
  const [comments, setComments] = useState(pin.comments || []);
  const [newComment, setNewComment] = useState('');
  const [selectedPin, setSelectedPin] = useState(null);
  const [showCommentInput, setShowCommentInput] = useState(false);
  const [selectedTemplate, setSelectedTemplate] = useState(null);

  useEffect(() => {
    const type = pin.type?.[0]; // 배열의 첫 번째 요소
    const selectedCategory = pin.selectedCategory?.[0]; // 배열의 첫 번째 요소
    const title = pin.title;
    const location = pin.location;
    const details = pin.details;
    console.log('Type:', type); // 디버깅을 위한 로그
    console.log('Selected Category:', selectedCategory);
    console.log('Title:', title);
    console.log('Location:', location);
    console.log('Details:', details);
    if (type && selectedCategory) {
      const TemplateComponent = templates[type]?.[selectedCategory];

      setSelectedTemplate(() => TemplateComponent);
    }
  }, [pin]);

  const closePinModal = () => setSelectedPin(null);
  const [comment, setComment] = useState('');

  const handleCommentSubmit = (e) => {
    e.preventDefault();
    console.log('Submitted comment:', comment);
    setComment('');
    // Here you would typically send the comment to your backend
  };

  const handleImageClick = (image) => {
    console.log('Clicked image URL:', image);
    const imageObject = {
        image: image,
        content: null
      };
    
      // 객체를 상태로 저장
      setSelectedPin(imageObject);    
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
  const rating = 4.5;
  const totalStars = 5;

  return (
    <div
    className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    onClick={handleOutsideClick}
    >
    {selectedPin && (
        <PinstoryModal pin={selectedPin} onClose={closePinModal} updatePin={null} />
    )}
      <div className="rounded-xl bg-white flex flex-col max-h-[60vh] overflow-y-auto scrollbar-hidden">
        <button
          className="flex justify-end text-xl font-bold text-gray-700 hover:text-gray-900 p-3"
          onClick={onClose}
        >×</button>
        {selectedTemplate ? (
          React.createElement(selectedTemplate, { data: pin.data })
        ) : (
          <p>{t('noTemplateFound')}</p>
        )}
      </div>
      <style jsx global>{`
        .scrollbar-hidden::-webkit-scrollbar {
          display: none;
        }
      `}</style>
    {/* <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md fixed max-h-[60vh] flex flex-col">
      {/* <div className="flex-1 overflow-hidden flex flex-col"> */}
        {/* Image grid section */}
        {/* <div className="flex items-center">
          <div className="flex items-right mb-4">
            <div className="h-20 w-20 grid grid-cols-2 gap-1">
              {(pin.images || []).map((image, index) => (
                <div key={index} className="h-10 w-10 relative">
                  <Image
                    src={image}
                    alt={`Image ${index}`}
                    layout="fill"
                    objectFit="cover"
                    className="rounded-sm cursor-pointer"
                    onClick={() => handleImageClick(image)}
                  />
                </div>
              ))}
              {pin.images?.length < 4 && 
                <div className="h-10 w-10 bg-gray-200 flex items-center justify-center text-gray-600 rounded-sm">
                  +
                </div>
              }
            </div>
          </div>
        </div> */}
      {/* </div> */}
      <div>
          <div className="flex justify-between items-start">
            {/* <div>
              <button className="mb-4 bg-blue-400 text-sm px-3 py-1 rounded-full">{t('cafeReview')}</button>
              <h3 className="mb-2 text-xl font-semibold">{t('cafeMoonlight')}</h3>
              <div className="flex items-center mb-4">
                {Array.from({ length: totalStars }, (v, i) => (
                  i < Math.floor(rating) ? 
                  <span key={i} className={`fas fa-star text-yellow-400`}></span> : null
                ))}
                {rating % 1 !== 0 && (
                  <span className="fas fa-star-half-alt text-yellow-400"></span>
                )}
                <span className="ml-2 text-gray-600">{rating.toFixed(1)}</span>
              </div>
            </div> */}
          </div>
        {/* <div className='mb-4'>
          {t('cafeDescription')}
        </div>
        <div className="flex gap-4 mb-4">
          <div>
            <div className='fa-brands fa-instagram mr-3'></div>
            <button>
              Instagram
            </button>
          </div>
          <div className="flex items-center justify-between">
            <div className="fa-brands fa-youtube mr-3"></div>
            <div>YouTube</div>
          </div>
        </div>
        <div className="flex items-center">
          <div className="fa-regular fa-heart mr-2"></div>
          <div className='mr-2'>23</div>
          <div className="fa-regular fa-comment mr-2" onClick={() => setShowCommentInput(!showCommentInput)} />    
          <div className='mr-2'>56</div>
          <div className="fas fa-share ml-auto"></div>
        </div>
      </div> */}
      {/* {showCommentInput && (
        <form onSubmit={handleCommentSubmit} className="flex space-x-2 mb-4 mt-2">
          <input
            type="text"
            placeholder={t('enterComment')}
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            className="flex-grow"
          />
          <button type="submit">{t('post')}</button>
        </form>
      )}
      {
        comments.length == 0 && showCommentInput ? t('noComments') :
          comments.map((comment, index) => (
            <div key={index} className="p-2 border border-gray-300 rounded-md">
              {comment}
            </div>
          ))
      }
    </div> */}
  </div>
  </div>
  );
};

export default PinLibraryModal;
