import { useState, useEffect } from 'react';
import Styles from '@/app/styles/PointDisplay.module.css';
const { useMyContext } = require("../context/myContext");

const PointDisplay = ({loc,time}) => {
    const { point } = useMyContext();
    const [usePoint, setUsePoint] = useState(10);
    const [residue, setResidue] = useState(point - usePoint);
    const [isAnimating, setIsAnimating] = useState(false);

    useEffect(() => {
        // Calculate the residue when point or usePoint changes
        setUsePoint(loc+time);
        setResidue(point - loc-time);
            // Trigger animation
    setIsAnimating(true);
    const timer = setTimeout(() => {
        setIsAnimating(false);
    }, 2000); // 2 seconds
    return () => clearTimeout(timer); // Cleanup timer on unmount
    }, [loc, time]); // Dependencies: update when point or usePoint changes

    return (
        <div className={`${Styles.pointContainer} ${isAnimating ? Styles.animate : ''}`}>
            <div className={Styles.pointText}>
                <h2 className="text-lg font-bold">포인트 사용 내역</h2>
                <div className="flex justify-between items-center border-t-2 border-white pt-1">
                    <p className="text-sm font-bold text-white text-opacity-90">현재 포인트</p>
                    <p className="text-lg font-extrabold">{point}</p>
                </div>
                <div className="flex justify-between items-center">
                    <p className="text-sm font-bold text-white text-opacity-90">차감 예정 포인트</p>
                    <p className="text-lg font-extrabold">{usePoint}</p>
                </div>
                <div className="flex flex-col items-end w-full">
                   <ul className="pl-2 w-2/3 list-none mt-1 w-full text-right border-t border-white border-opacity-60"> {/* 너비를 부모의 2/3으로 설정하고 오른쪽 정렬 */}
                        {loc !== 0 && ( // loc이 0이 아닐 때만 렌더링
                            <li className="text-sm flex justify-between w-full mb-1">
                                <span>거리 설정:</span>
                                <span className="font-light">{loc}</span>
                            </li>
                        )}
                        <li className="text-sm flex justify-between w-full mb-1">
                            <span>배송 옵션</span>
                            <span className="font-light">{time}</span>
                        </li>
                    </ul>
                </div>
                <div className="flex justify-between items-center border-t-2 border-white pt-1">
                    <p className="text-sm font-bold text-white text-opacity-90">잔여 포인트</p>
                    <p className="text-lg font-extrabold">{residue}</p>
                </div>
            </div>
        </div>
    );
};

export default PointDisplay;