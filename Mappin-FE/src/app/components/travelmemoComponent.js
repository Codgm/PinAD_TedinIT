import { useState } from 'react';
import styles from '@/app/styles/EventTemplete.module.css';

const TravelmemoComponent = () => {
  const [travelData, setTravelData] = useState({
    title: '',
    location: '',
    date: '',
    event: '',
    sight: '',
    sound: '',
    touch: '',
    smell: '',
    taste: '',
    memorable_moment: '',
    peopleMet: '',
    unexpected_discovery: '',
    self_at_place: '',
    thoughts: [''], // 시작할 때 하나의 빈 문자열로 시작
    final_message: ''
  });

  // Handle input change for general fields
  const handleChange = (e) => {
    const { name, value } = e.target;
    setTravelData((prevData) => ({ ...prevData, [name]: value }));
  };

  // Handle input change for thoughts fields
  const handleThoughtChange = (index, value) => {
    const updatedThoughts = [...travelData.thoughts];
    updatedThoughts[index] = value;
    setTravelData((prevData) => ({ ...prevData, thoughts: updatedThoughts }));
  };

  // Add new thought field on Enter key press
  const handleKeyDown = (index, event) => {
    if (event.key === 'Enter' && index === travelData.thoughts.length - 1) {
      event.preventDefault(); // Prevent default behavior (newline)
      setTravelData((prevData) => ({
        ...prevData,
        thoughts: [...prevData.thoughts, ''] // Add new empty thought field
      }));
    }
  };

  // Remove a specific thought
  const handleRemoveThought = (index) => {
    if (travelData.thoughts.length > 1) {
      const updatedThoughts = travelData.thoughts.filter((_, i) => i !== index);
      setTravelData((prevData) => ({ ...prevData, thoughts: updatedThoughts }));
    }
  };

  // Remove empty thoughts from the array
  const handleRemoveEmptyThoughts = () => {
    setTravelData((prevData) => ({
      ...prevData,
      thoughts: prevData.thoughts.filter(thought => thought.trim() !== '')
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    handleRemoveEmptyThoughts(); // Clean up before submitting
    // Submit processing logic
    console.log(travelData);
  };

  return (
    <div className={styles.container}>
      <form onSubmit={handleSubmit}>
        <div className='flex items-center w-full space-x-2'>
          <div className={styles.formGroup}>
            <input
              type="text"
              id="title"
              name="title"
              placeholder="장소 이름"
              value={travelData.title}
              onChange={handleChange}
              className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
              required
            />
          </div>
          <div className={styles.formGroup}>
            <input
              type="text"
              id="location"
              name="location"
              placeholder="장소 위치"
              value={travelData.location}
              onChange={handleChange}
              className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
              required
            />
          </div>
        </div>

        <div className={styles.formGroup}>
          <input
            type="datetime-local"
            id="date"
            name="date"
            placeholder='날짜'
            value={travelData.date}
            onChange={handleChange}
            required
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="event" className="block text-sm font-medium text-gray-700">순간의 감정</label>
          <input
            id="event"
            name="event"
            value={travelData.event}
            onChange={handleChange}
            placeholder='이 순간 느낌 감정을 한 문장으로..'
            className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            required
          />
        </div>

        <div className="p-6 max-w-lg mx-auto bg-white rounded-lg shadow-lg border border-gray-300">
          <h3 className="text-lg font-bold mb-4">오감으로 담아낸 이곳</h3>
          <div className="mb-4">
            <textarea
              id="sight"
              name="sight"
              placeholder="시각"
              value={travelData.sight}
              onChange={handleChange}
              rows="1"
              className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            />
          </div>

          <div className="mb-4">
            <textarea
              id="sound"
              name="sound"
              placeholder='청각'
              value={travelData.sound}
              onChange={handleChange}
              rows="1"
              className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            />
          </div>

          <div className="mb-4">
            <textarea
              id="touch"
              name="touch"
              placeholder='촉각'
              value={travelData.touch}
              onChange={handleChange}
              rows="1"
              className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            />
          </div>

          <div className="mb-4">
            <textarea
              id="smell"
              name="smell"
              placeholder='후각'
              value={travelData.smell}
              onChange={handleChange}
              rows="1"
              className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            />
          </div>

          <div className="mb-4">
            <textarea
              id="taste"
              name="taste"
              placeholder='미각'
              value={travelData.taste}
              onChange={handleChange}
              rows="1"
              className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
            />
          </div>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="memorable_moment" className="block text-sm font-medium text-gray-700">마음에 세겨진 한컷</label>
          <input
            id="memorable_moment"
            name="memorable_moment"
            placeholder='가장 인상 깊은 장면,순간'
            value={travelData.memorable_moment}
            onChange={handleChange}
            rows="1"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        <div className={styles.formGroup}>
          <input
            id="peopleMet"
            name="peopleMet"
            placeholder='특별히 기억에 남는 사람,대화'
            value={travelData.peopleMet}
            onChange={handleChange}
            rows="1"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        <div className={styles.formGroup}>
          <input
            id="unexpected_discovery"
            name="unexpected_discovery"
            placeholder='예상치 못한 발견,경험'
            value={travelData.unexpected_discovery}
            onChange={handleChange}
            rows="3"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        <div className={styles.formGroup}>
          <textarea
            id="self_at_place"
            name="self_at_place"
            placeholder='이곳에서의 나의 감정,생각,변화'
            value={travelData.self_at_place}
            onChange={handleChange}
            rows="3"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        {travelData.thoughts.length > 0 && (
          <div className={styles.formGroup}>
            <label htmlFor="thoughts" className="block text-sm font-medium text-gray-700">떠오르는 생각들</label>
            {travelData.thoughts.map((thought, index) => (
              <div key={index} className="flex items-center mb-2">
                <input
                  value={thought}
                  onChange={(e) => handleThoughtChange(index, e.target.value)}
                  onKeyDown={(e) => handleKeyDown(index, e)}
                  rows="1"
                  placeholder={`생각 ${index + 1}`}
                  className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
                />
                {index != 0 ? (
                  <button
                    type="button"
                    onClick={() => handleRemoveThought(index)}
                    className="ml-2 text-red-300 hover:text-red-600"
                  >
                    ×
                  </button>
                ):(
                  <button
                    type="button"
                    className="ml-2 text-white"
                  >
                    x
                  </button>
                )}
              </div>
            ))}
            <span className="ml-3 block text-sm font-medium text-gray-400">생각을 적어보아요! 생각을 더 적고 싶으면 엔터를 눌러 더 작성할 수 있어요</span>
          </div>
        )}

        <div className={styles.formGroup}>
          <label htmlFor="final_message" className="block text-sm font-medium text-gray-700">남기고 싶은 한마디</label>
          <input
            id="final_message"
            name="final_message"
            value={travelData.final_message}
            onChange={handleChange}
            rows="3"
            className="mt-1 block w-full bg-white border-b border-gray-300 focus:border-indigo-500 focus:ring-0 sm:text-sm"
          />
        </div>

        <button
          type="submit"
          className="mt-4 px-4 py-2 bg-indigo-600 text-white font-semibold rounded-md shadow-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          제출
        </button>
      </form>
    </div>
  );
};

export default TravelmemoComponent;
