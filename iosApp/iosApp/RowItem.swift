import SwiftUI
import SharedAppLib
import Kingfisher
import SharedAppLib


/*let article = Article(
    url: Url(impl: URL(string: "https://www.google.com")!),
    title: Title(value: "Clubhouse is now available for IOS"),
    author: Author(value: "Max Oliynick"),
    description: Description(value: """
Let your imagination fly! Modifiers let you modify your composable in a very flexible way. For example, if you wanted to add some outer spacing, change the background color of the composable, and round the corners of the Row, you could use the following code
"""),
    urlToImage: Url.init(impl: URL(string: "https://miro.medium.com/max/4000/1*Ir8CdY5D5Do5R_22Vo3uew.png")!),
    published: CommonDate(impl: Date()),
    isFavorite: true
)

let articles = [article, article, article]*/

class ArticlesViewModel: ObservableObject {
    
    /*enum LoadableLaunches {
        case loading
        case result([RocketLaunch])
        case error(String)
    }*/
    
    @Published var articles: [Article] = [Article]()
    
    private let newsApi: NewsApiCommon
    
    init(newsApi: NewsApiCommon) {
        self.newsApi = newsApi
        load()
    }
    
    func load() {
        
        let wrapper = IosComponentWrapper.init(env: EnvironmentKt.PlatformEnv(closeCommandsFlow: { close in
            print("Close app \(close)")
        }))
        
        wrapper.render { state in
            print("New app state \(state)")
            
            state.screen
            
        }
        wrapper.send(message: ToggleDarkMode.init())
        
            /*newsApi.fetchFromEverything(input: "IOS new", currentSize: 0, resultsPerPage: 20) { (page, error) in
            
            if let page = page {
                self.articles = page.articles
            }
            
            if error != nil {
                print("Oh shit \(error)")
            } else {
                print("ZBS \(page)")
            }
        }*/
        
    }
    
}

struct ArticlesView: View {
    
    @ObservedObject private(set) var viewModel: ArticlesViewModel
    
     //var articles: [Article] = [Article]()
    
    var body: some View {
        

        List(viewModel.articles, id: \.url) { article in
            RowItem(article: article)
        }.navigationBarItems(trailing:
                                Button("Reload") {
                                    self.viewModel.load()                            })
    }
}

struct RowItem: View {
    
    private let dateFormatter = DateFormatter()
    
    let article: Article

    init(article: Article) {
        dateFormatter.dateFormat = "dd MMM' at 'hh:mm"
        self.article = article
    }
    
    var body: some View {
    
        
        VStack(alignment: HorizontalAlignment.leading, spacing: /*@START_MENU_TOKEN@*/nil/*@END_MENU_TOKEN@*/, content: {
            
            if let image = article.urlToImage {
                KFImage.url(image)
                    .resizable()
                    .fade(duration: 0.25)
                    .aspectRatio(contentMode: .fit)
                    .frame(width: .infinity, height: 200, alignment: Alignment.center)
            }
            // todo get rid of casts
            Text(article.title as! String)
            
            if let author = article.author as? String {
                Text("By \(author)")
                    .font(.caption)
            }
            
            if let description = article.description_ as? String {
                Text(description).font(.subheadline).lineLimit(100)
            }
            
            Text("Published on \(dateFormatter.string(from: article.published))")
                .font(.caption)
        }).padding(
            EdgeInsets(
                top: CGFloat(16.0),
                leading: CGFloat(16.0),
                bottom: CGFloat(16.0),
                trailing: CGFloat(16.0)
            )
        )
    }
}



/*struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        //RowItem(article: article)
    }
}*/
