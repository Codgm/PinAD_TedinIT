
import React, { useState } from 'react';
import '@fortawesome/fontawesome-free/css/all.min.css';
import PinstoryModal from './pinstorymodal';
import Image from 'next/image';


const PinLibraryModal = ({ pin, onClose, updatePin }) => {
  const [likeCount, setLikeCount] = useState(pin.likes || 0);
  const [comments, setComments] = useState(pin.comments || []);
  const [newComment, setNewComment] = useState('');
  const [selectedPin, setSelectedPin] = useState(null);
  
  const closePinModal = () => setSelectedPin(null);


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
      <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold">사용자의 핀스토리</h2>
          <button
            className="text-xl font-bold text-gray-700 hover:text-gray-900"
            onClick={onClose}
          >×</button>
        </div>
        <div>
          <div className="flex justify-between items-start">
            <div>
              <button className="mb-4 bg-blue-400 text-sm px-3 py-1 rounded-full">#카페후기</button>
              <h3 className="mb-2 text-xl font-semibold">카페 달빛</h3>
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
            </div>
            <div className="flex items-center">
              <div className="h-20 w-20 grid grid-cols-2 gap-1">
                {pin.images.map((image, index) => (
                  <div key={index} className="h-10 w-10 relative">
                    <Image
                      src={image}
                      alt={`Image ${index}`}
                      layout="fill"
                      objectFit="cover"
                      className="rounded-sm"
                      onClick={() => handleImageClick(image)}
                    />
                  </div>
                ))}          
              </div> 
            </div>
          </div> 
        <div>
          이 카페는 정말 특별해요! 창밖으로 보이는 도시 전경이 환상적이에요. 
          특히 해질 무렵에 오면 노을과 함께 커피를 즐길 수 있어요. 
          시그니처 메뉴인 달빛 라떼는 꼭 드셔보세요. 부드러운 우유 거품 위에 은은한 바닐라 향이 어우러져 정말 맛있어요. 
          조용히 책을 읽거나 여유롭게 대화를 나누기에 완벽한 공간이에요.
        </div>
        <div className="mb-4">
            {/* {!pin.images || pin.images.length === 0 ? (
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
            )} */}
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
          <div className="fa-regular fa-comment mr-2"></div>    
          <div>56</div>
        </div>
      </div>
    </div>
  </div>
    );
};

export default PinLibraryModal;
