import React, { useState } from 'react';
import Image from 'next/image';
import Draggable from 'react-draggable';
import Styles from '@/app/styles/MapExample.module.css';

const MapExample = () => {
  const [markerPosition, setMarkerPosition] = useState({ top: 50, left: 50 }); 

  const handleDrag = (e, ui) => {
    const { x, y } = ui;
    setMarkerPosition({ top: y, left: x });
  };

  return (
    <div className={Styles.mapContainer}>
      <Image
        src="/gangnam.png"
        alt="Map"
        fill
        style={{ objectFit: 'cover' }}
      />
      <div className={Styles.mapBackground}>
        <Draggable
          position={{ x: markerPosition.left, y: markerPosition.top }}
          onDrag={handleDrag}
          bounds="parent"
        >
          <div className={Styles.mapMarker}>
            📍
          </div>
        </Draggable>
      </div>
    </div>
  );
};

export default MapExample;
