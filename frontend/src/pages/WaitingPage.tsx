
function WaitingPage() {

    return (
        <div className="flex w-full h-full flex-col items-center justify-center-safe ">
            <div className="bg-quiz-white w-lg h-128 rounded-lg shadow-md flex flex-col items-center justify-center gap-8">
                <img src='/src/assets/logo.svg' className='h-32 w-32 lg:h-64 lg:w-64 animate-spin [animation-duration:1.5s]' />
                <div className="text-4xl text-quiz-green">WAITING FOR START :D</div>
            </div>
        </div>
    )
}

export default WaitingPage;