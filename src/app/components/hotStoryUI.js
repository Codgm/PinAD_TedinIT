import React from 'react';

const imageOptions = [
  '/노래방.jpg',
  '/삼겹살.jpg',
  '/profileimg3.jpg',
  '/profileimg2.jpg'
];

const getRandomImage = () => {
  const randomIndex = Math.floor(Math.random() * imageOptions.length);
  return imageOptions[randomIndex];
};
const images = Array.from({ length: 20 }, getRandomImage);

const NewUI = () => {
  return (
    <div className="bg-gradient-to-r from-blue-900 to-purple-900 p-4">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 auto-rows-[200px] grid-flow-dense">
        {images.map((src, index) => {
          let spanCols = 'col-span-1';
          let spanRows = 'row-span-1';
          
          if (index % 6 === 0) spanCols = 'col-span-2'; // every 6th image spans 2 columns
          if (index % 7 === 0) spanRows = 'row-span-2'; // every 7th image spans 2 rows
          if (index % 5 === 0) { // additional big images
            spanCols = 'col-span-2';
            spanRows = 'row-span-2';
          }

          return (
            <div key={index} className={`relative ${spanCols} ${spanRows}`}>
              <img src={src} alt={`Image ${index}`} className="w-full h-full object-cover rounded-lg" />
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default NewUI;
